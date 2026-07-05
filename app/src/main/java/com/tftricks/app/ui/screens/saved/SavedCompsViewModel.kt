package com.tftricks.app.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.SavedTeam
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavedCompsContent(
    val favoriteComps: List<TeamComp>,
    val savedTeams: List<SavedTeam>,
    /** Champion lookup by id for rendering saved team units. */
    val championsById: Map<String, Champion>
)

class SavedCompsViewModel(private val container: AppContainer) : ViewModel() {

    private val data = MutableStateFlow<Pair<List<TeamComp>, Map<String, Champion>>?>(null)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<SavedCompsContent>> = combine(
        data,
        error,
        container.favoritesRepository.favorites(FavoriteCategory.COMP),
        container.savedTeamsRepository.savedTeams
    ) { loaded, err, favorites, teams ->
        when {
            err != null -> UiState.Error(err)
            loaded == null -> UiState.Loading
            else -> UiState.Success(
                SavedCompsContent(
                    favoriteComps = loaded.first.filter { it.id in favorites }.sortedBy { it.tier },
                    savedTeams = teams.sortedByDescending { it.createdAt },
                    championsById = loaded.second
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            try {
                data.value = container.teamCompRepository.getTeamComps() to
                    container.championRepository.getChampions().associateBy { it.id }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load saved comps"
            }
        }
    }

    fun unfavorite(compId: String) {
        viewModelScope.launch {
            container.favoritesRepository.toggle(FavoriteCategory.COMP, compId)
        }
    }

    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            container.savedTeamsRepository.deleteTeam(teamId)
        }
    }
}
