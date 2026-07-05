package com.tftricks.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Aggregated numbers proving the whole data pipeline works. */
data class HomeOverview(
    val currentPatch: String,
    val topComps: List<TeamComp>,
    val championCount: Int,
    val traitCount: Int,
    val itemCount: Int,
    val augmentCount: Int
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeOverview>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeOverview>> = _uiState.asStateFlow()

    init {
        loadOverview()
    }

    private fun loadOverview() {
        viewModelScope.launch {
            _uiState.value = try {
                val comps = container.teamCompRepository.getTeamComps()
                val patchNotes = container.patchNoteRepository.getPatchNotes()
                UiState.Success(
                    HomeOverview(
                        currentPatch = patchNotes.maxByOrNull { it.date }?.version ?: "—",
                        topComps = comps.sortedBy { it.tier }.take(3),
                        championCount = container.championRepository.getChampions().size,
                        traitCount = container.traitRepository.getTraits().size,
                        itemCount = container.itemRepository.getItems().size,
                        augmentCount = container.augmentRepository.getAugments().size
                    )
                )
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load data")
            }
        }
    }
}
