package com.tftricks.app.ui.screens.traits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.Trait
import com.tftricks.app.domain.repository.TraitRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TraitsViewModel(private val repository: TraitRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Trait>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Trait>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                UiState.Success(repository.getTraits().sortedBy { it.name })
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load traits")
            }
        }
    }
}
