package com.tftricks.app.ui.screens.traits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.Trait
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.domain.repository.TraitRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TraitsContent(
    val traits: List<Trait>,
    /** Champion lookup by display name for cost colors and navigation. */
    val championsByName: Map<String, Champion>
)

class TraitsViewModel(
    private val repository: TraitRepository,
    private val championRepository: ChampionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<TraitsContent>>(UiState.Loading)
    val uiState: StateFlow<UiState<TraitsContent>> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = try {
                UiState.Success(
                    TraitsContent(
                        traits = repository.getTraits().sortedBy { it.name },
                        championsByName = championRepository.getChampions().associateBy { it.name }
                    )
                )
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load traits")
            }
        }
    }

    fun retry() = load()
}
