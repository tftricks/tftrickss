package com.tftricks.app.ui.screens.champions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChampionsViewModel(private val repository: ChampionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Champion>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Champion>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                UiState.Success(repository.getChampions().sortedWith(compareBy({ it.cost }, { it.name })))
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load champions")
            }
        }
    }
}
