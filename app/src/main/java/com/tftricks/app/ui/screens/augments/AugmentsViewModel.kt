package com.tftricks.app.ui.screens.augments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.Augment
import com.tftricks.app.domain.repository.AugmentRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AugmentsViewModel(private val repository: AugmentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Augment>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Augment>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                UiState.Success(
                    repository.getAugments()
                        .sortedWith(compareByDescending<Augment> { it.priorityRating }.thenBy { it.name })
                )
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load augments")
            }
        }
    }
}
