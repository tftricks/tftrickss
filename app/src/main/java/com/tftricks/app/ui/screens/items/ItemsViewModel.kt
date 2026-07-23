package com.tftricks.app.ui.screens.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.ItemCategory
import com.tftricks.app.domain.repository.FavoritesRepository
import com.tftricks.app.domain.repository.ItemRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A completed item recipe: which two components build it. */
data class ComboEntry(
    val componentA: String,
    val componentB: String,
    val result: Item
)

data class ItemsContent(
    val items: List<Item>,
    val selectedCategory: ItemCategory?,
    val favoriteIds: Set<String>,
    /** All base components appearing in recipes, sorted. */
    val components: List<String>,
    /** All two-component recipes in the database. */
    val combos: List<ComboEntry>
)

class ItemsViewModel(
    private val repository: ItemRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val allItems = MutableStateFlow<List<Item>?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val selectedCategory = MutableStateFlow<ItemCategory?>(null)

    val uiState: StateFlow<UiState<ItemsContent>> = combine(
        allItems, error, selectedCategory,
        favoritesRepository.favorites(FavoriteCategory.ITEM)
    ) { items, err, category, favorites ->
        when {
            err != null -> UiState.Error(err)
            items == null -> UiState.Loading
            else -> {
                val combos = items
                    .filter { it.components.size == 2 }
                    .map { ComboEntry(it.components[0], it.components[1], it) }
                UiState.Success(
                    ItemsContent(
                        items = items
                            .filter { category == null || it.category == category }
                            .sortedBy { it.name },
                        selectedCategory = category,
                        favoriteIds = favorites,
                        components = combos
                            .flatMap { listOf(it.componentA, it.componentB) }
                            .distinct()
                            .sorted(),
                        combos = combos
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            error.value = null
            try {
                allItems.value = repository.getItems()
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load items"
            }
        }
    }

    fun retry() = load()

    fun selectCategory(category: ItemCategory?) {
        selectedCategory.value = if (selectedCategory.value == category) null else category
    }

    fun toggleFavorite(itemId: String) {
        viewModelScope.launch { favoritesRepository.toggle(FavoriteCategory.ITEM, itemId) }
    }
}
