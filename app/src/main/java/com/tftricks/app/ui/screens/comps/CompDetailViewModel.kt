package com.tftricks.app.ui.screens.comps

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

data class CompDetailContent(
    val comp: TeamComp,
    val isFavorite: Boolean,
    /** Champion lookup by display name, for cost colors and navigation from boards. */
    val championsByName: Map<String, Champion>,
    /** Item id lookup by display name, for navigation from item chips. */
    val itemIdsByName: Map<String, String>
)

class CompDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer
) : ViewModel() {

    private val compId: String = checkNotNull(savedStateHandle[DetailRoutes.COMP_ARG])

    private val data = MutableStateFlow<Triple<TeamComp, Map<String, Champion>, Map<String, String>>?>(null)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<CompDetailContent>> = combine(
        data, error, container.favoritesRepository.favorites(FavoriteCategory.COMP)
    ) { loaded, err, favorites ->
        when {
            err != null -> UiState.Error(err)
            loaded == null -> UiState.Loading
            else -> UiState.Success(
                CompDetailContent(
                    comp = loaded.first,
                    isFavorite = compId in favorites,
                    championsByName = loaded.second,
                    itemIdsByName = loaded.third
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            try {
                val comp = container.teamCompRepository.getTeamComp(compId)
                if (comp == null) {
                    error.value = "Comp not found"
                } else {
                    val champions = container.championRepository.getChampions()
                    val items = container.itemRepository.getItems()
                    data.value = Triple(
                        comp,
                        champions.associateBy { it.name },
                        items.associate { it.name to it.id }
                    )
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load comp"
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            container.favoritesRepository.toggle(FavoriteCategory.COMP, compId)
        }
    }
}
