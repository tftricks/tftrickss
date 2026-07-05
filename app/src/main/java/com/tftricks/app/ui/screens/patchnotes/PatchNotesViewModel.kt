package com.tftricks.app.ui.screens.patchnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.PatchNote
import com.tftricks.app.domain.repository.PatchNoteRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PatchNotesViewModel(private val repository: PatchNoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<PatchNote>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<PatchNote>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                UiState.Success(repository.getPatchNotes().sortedByDescending { it.date })
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load patch notes")
            }
        }
    }
}
