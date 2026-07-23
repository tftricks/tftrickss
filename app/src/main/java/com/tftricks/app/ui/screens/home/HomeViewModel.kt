package com.tftricks.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.Tier
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeContent(
    val currentPatch: String,
    /** S-tier comps featured on the dashboard. */
    val featuredComps: List<TeamComp>,
    val favoriteComps: List<TeamComp>,
    val favoriteChampions: List<Champion>,
    val favoriteItems: List<Item>
)

private data class HomeData(
    val patch: String,
    val comps: List<TeamComp>,
    val champions: List<Champion>,
    val items: List<Item>
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val data = MutableStateFlow<HomeData?>(null)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<HomeContent>> = combine(
        data,
        error,
        container.favoritesRepository.favorites(FavoriteCategory.COMP),
        container.favoritesRepository.favorites(FavoriteCategory.CHAMPION),
        container.favoritesRepository.favorites(FavoriteCategory.ITEM)
    ) { loaded, err, favComps, favChampions, favItems ->
        when {
            err != null -> UiState.Error(err)
            loaded == null -> UiState.Loading
            else -> UiState.Success(
                HomeContent(
                    currentPatch = loaded.patch,
                    featuredComps = loaded.comps.filter { it.tier == Tier.S }
                        .ifEmpty { loaded.comps.sortedBy { it.tier }.take(2) },
                    favoriteComps = loaded.comps.filter { it.id in favComps },
                    favoriteChampions = loaded.champions.filter { it.id in favChampions },
                    favoriteItems = loaded.items.filter { it.id in favItems }
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
                // Patch notes and comps are bundled locally; champions/items are fetched
                // live and shouldn't block the dashboard's bundled content if that fails.
                val champions = runCatching { container.championRepository.getChampions() }
                    .getOrDefault(emptyList())
                val items = runCatching { container.itemRepository.getItems() }
                    .getOrDefault(emptyList())
                data.value = HomeData(
                    patch = container.patchNoteRepository.getPatchNotes()
                        .maxByOrNull { it.date }?.version ?: "—",
                    comps = container.teamCompRepository.getTeamComps(),
                    champions = champions,
                    items = items
                )
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load data"
            }
        }
    }

    fun retry() = load()
}
