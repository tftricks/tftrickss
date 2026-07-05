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
    /** Comps from [Champion.bestComps], resolved so cards can show names and navigate. */
    val bestComps: List<TeamComp>,
    /** Item id lookup by display name, for navigation from recommended items. */
    val itemIdsByName: Map<String, String>
)

class ChampionDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer
) : ViewModel() {

    private val championId: String = checkNotNull(savedStateHandle[DetailRoutes.CHAMPION_ARG])

    private val data = MutableStateFlow<Triple<Champion, List<TeamComp>, Map<String, String>>?>(null)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<ChampionDetailContent>> = combine(
        data, error, container.favoritesRepository.favorites(FavoriteCategory.CHAMPION)
    ) { loaded, err, favorites ->
        when {
            err != null -> UiState.Error(err)
            loaded == null -> UiState.Loading
            else -> UiState.Success(
                ChampionDetailContent(
                    champion = loaded.first,
                    isFavorite = championId in favorites,
                    bestComps = loaded.second,
                    itemIdsByName = loaded.third
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            try {
                val champion = container.championRepository.getChampion(championId)
                if (champion == null) {
                    error.value = "Champion not found"
                } else {
                    val comps = container.teamCompRepository.getTeamComps()
                        .filter { it.id in champion.bestComps }
                    val items = container.itemRepository.getItems()
                    data.value = Triple(champion, comps, items.associate { it.name to it.id })
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load champion"
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            container.favoritesRepository.toggle(FavoriteCategory.CHAMPION, championId)
        }
    }
}
