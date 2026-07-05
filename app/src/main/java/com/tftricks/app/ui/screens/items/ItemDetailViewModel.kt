package com.tftricks.app.ui.screens.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.Item
import com.tftricks.app.ui.common.UiState
import com.tftricks.app.ui.navigation.DetailRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ItemDetailContent(
    val item: Item,
    val isFavorite: Boolean,
    /** Champion id lookup by display name, for navigation from best users. */
    val championIdsByName: Map<String, String>,
    /** Item id lookup by display name, for navigation from alternatives. */
    val itemIdsByName: Map<String, String>
)

class ItemDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle[DetailRoutes.ITEM_ARG])

    private val data = MutableStateFlow<Triple<Item, Map<String, String>, Map<String, String>>?>(null)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<ItemDetailContent>> = combine(
        data, error, container.favoritesRepository.favorites(FavoriteCategory.ITEM)
    ) { loaded, err, favorites ->
        when {
            err != null -> UiState.Error(err)
            loaded == null -> UiState.Loading
            else -> UiState.Success(
                ItemDetailContent(
                    item = loaded.first,
                    isFavorite = itemId in favorites,
                    championIdsByName = loaded.second,
                    itemIdsByName = loaded.third
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            try {
                val item = container.itemRepository.getItem(itemId)
                if (item == null) {
                    error.value = "Item not found"
                } else {
                    val champions = container.championRepository.getChampions()
                    val items = container.itemRepository.getItems()
                    data.value = Triple(
                        item,
                        champions.associate { it.name to it.id },
                        items.associate { it.name to it.id }
                    )
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load item"
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            container.favoritesRepository.toggle(FavoriteCategory.ITEM, itemId)
        }
    }
}
