package com.tftricks.app.ui.screens.patchnotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.PatchNote
import com.tftricks.app.domain.repository.PatchNoteRepository
import com.tftricks.app.ui.common.UiState
import com.tftricks.app.ui.navigation.DetailRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PatchNoteDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PatchNoteRepository
) : ViewModel() {

    private val patchId: String = checkNotNull(savedStateHandle[DetailRoutes.PATCH_ARG])

    private val _uiState = MutableStateFlow<UiState<PatchNote>>(UiState.Loading)
    val uiState: StateFlow<UiState<PatchNote>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                val patch = repository.getPatchNotes().find { it.id == patchId }
                if (patch == null) UiState.Error("Patch not found")
                else UiState.Success(patch)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load patch")
            }
        }
    }
}
