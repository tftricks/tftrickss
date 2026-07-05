package com.tftricks.app.ui.screens.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.repository.ItemRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemsViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Item>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Item>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                UiState.Success(repository.getItems().sortedBy { it.name })
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load items")
            }
        }
    }
}
