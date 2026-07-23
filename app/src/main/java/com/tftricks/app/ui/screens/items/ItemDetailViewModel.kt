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
    /** Champions this item is built on across bundled comps, in order of first appearance. */
    val bestUsers: List<String>,
    /** Champion id lookup by display name, for navigation from best users. */
    val championIdsByName: Map<String, String>
)

class ItemDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle[DetailRoutes.ITEM_ARG])

    private data class Loaded(
        val item: Item,
        val bestUsers: List<String>,
        val championIdsByName: Map<String, String>
    )

    private val data = MutableStateFlow<Loaded?>(null)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<ItemDetailContent>> = combine(
        data, error, container.favoritesRepository.favorites(FavoriteCategory.ITEM)
    ) { loaded, err, favorites ->
        when {
            err != null -> UiState.Error(err)
            loaded == null -> UiState.Loading
            else -> UiState.Success(
                ItemDetailContent(
                    item = loaded.item,
                    isFavorite = itemId in favorites,
                    bestUsers = loaded.bestUsers,
                    championIdsByName = loaded.championIdsByName
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            error.value = null
            try {
                val item = container.itemRepository.getItem(itemId)
                if (item == null) {
                    error.value = "Item not found"
                } else {
                    val comps = container.teamCompRepository.getTeamComps()
                    val bestUsers = mutableListOf<String>()
                    comps.forEach { comp ->
                        (comp.finalBoard + comp.variants.flatMap { it.finalBoard })
                            .filter { item.name in it.items }
                            .forEach { unit -> if (unit.champion !in bestUsers) bestUsers += unit.champion }
                    }
                    val champions = container.championRepository.getChampions()
                    data.value = Loaded(
                        item = item,
                        bestUsers = bestUsers,
                        championIdsByName = champions.associate { it.name to it.id }
                    )
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load item"
            }
        }
    }

    fun retry() = load()

    fun toggleFavorite() {
        viewModelScope.launch {
            container.favoritesRepository.toggle(FavoriteCategory.ITEM, itemId)
        }
    }
}
