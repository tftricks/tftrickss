package com.tftricks.app.ui.screens.comps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.repository.TeamCompRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamCompsViewModel(private val repository: TeamCompRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<TeamComp>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<TeamComp>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                UiState.Success(repository.getTeamComps().sortedBy { it.tier })
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load team comps")
            }
        }
    }
}
