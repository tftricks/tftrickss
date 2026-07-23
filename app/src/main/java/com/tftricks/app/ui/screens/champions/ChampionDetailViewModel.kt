package com.tftricks.app.ui.screens.champions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.ui.common.UiState
import com.tftricks.app.ui.navigation.DetailRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChampionDetailContent(
    val champion: Champion,
    val isFavorite: Boolean,
    /** Bundled comps that field this champion, resolved so cards can show names and navigate. */
    val bestComps: List<TeamComp>,
    /** Items this champion is built with across those comps, in order of first appearance. */
    val bestItems: List<String>,
    /** Item id lookup by display name, for navigation from best items. */
    val itemIdsByName: Map<String, String>
)

class ChampionDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer
) : ViewModel() {

    private val championId: String = checkNotNull(savedStateHandle[DetailRoutes.CHAMPION_ARG])

    private data class Loaded(
        val champion: Champion,
        val bestComps: List<TeamComp>,
        val bestItems: List<String>,
        val itemIdsByName: Map<String, String>
    )

    private val data = MutableStateFlow<Loaded?>(null)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<ChampionDetailContent>> = combine(
        data, error, container.favoritesRepository.favorites(FavoriteCategory.CHAMPION)
    ) { loaded, err, favorites ->
        when {
            err != null -> UiState.Error(err)
            loaded == null -> UiState.Loading
            else -> UiState.Success(
                ChampionDetailContent(
                    champion = loaded.champion,
                    isFavorite = championId in favorites,
                    bestComps = loaded.bestComps,
                    bestItems = loaded.bestItems,
                    itemIdsByName = loaded.itemIdsByName
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
                val champion = container.championRepository.getChampion(championId)
                if (champion == null) {
                    error.value = "Champion not found"
                } else {
                    val allComps = container.teamCompRepository.getTeamComps()
                    val bestComps = allComps.filter { comp -> comp.references(championId = champion.name) }
                    val bestItems = mutableListOf<String>()
                    bestComps.forEach { comp ->
                        (comp.finalBoard + comp.variants.flatMap { it.finalBoard })
                            .filter { it.champion == champion.name }
                            .forEach { unit ->
                                unit.items.forEach { item -> if (item !in bestItems) bestItems += item }
                            }
                    }
                    val items = container.itemRepository.getItems()
                    data.value = Loaded(
                        champion = champion,
                        bestComps = bestComps,
                        bestItems = bestItems,
                        itemIdsByName = items.associate { it.name to it.id }
                    )
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load champion"
            }
        }
    }

    fun retry() = load()

    fun toggleFavorite() {
        viewModelScope.launch {
            container.favoritesRepository.toggle(FavoriteCategory.CHAMPION, championId)
        }
    }

    /** True if [championId] appears anywhere in this comp's roster, including its variants. */
    private fun TeamComp.references(championId: String): Boolean {
        val names = buildSet {
            finalBoard.forEach { add(it.champion) }
            earlyGameBoard.forEach { add(it.champion) }
            midGameBoard.forEach { add(it.champion) }
            addAll(carryChampions)
            addAll(tankChampions)
            variants.forEach { variant ->
                variant.finalBoard.forEach { add(it.champion) }
                addAll(variant.carryChampions)
                addAll(variant.tankChampions)
            }
        }
        return championId in names
    }
}
