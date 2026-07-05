package com.tftricks.app.ui.screens.champions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.domain.repository.FavoritesRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChampionsContent(
    val champions: List<Champion>,
    val allTraits: List<String>,
    val selectedCost: Int?,
    val selectedTrait: String?,
    val favoriteIds: Set<String>
)

class ChampionsViewModel(
    private val repository: ChampionRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val allChampions = MutableStateFlow<List<Champion>?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val selectedCost = MutableStateFlow<Int?>(null)
    private val selectedTrait = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<ChampionsContent>> = combine(
        allChampions, error, selectedCost, selectedTrait,
        favoritesRepository.favorites(FavoriteCategory.CHAMPION)
    ) { champions, err, cost, trait, favorites ->
        when {
            err != null -> UiState.Error(err)
            champions == null -> UiState.Loading
            else -> UiState.Success(
                ChampionsContent(
                    champions = champions
                        .filter { cost == null || it.cost == cost }
                        .filter { trait == null || trait in it.traits }
                        .sortedWith(compareBy({ it.cost }, { it.name })),
                    allTraits = champions.flatMap { it.traits }.distinct().sorted(),
                    selectedCost = cost,
                    selectedTrait = trait,
                    favoriteIds = favorites
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            try {
                allChampions.value = repository.getChampions()
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load champions"
            }
        }
    }

    fun selectCost(cost: Int?) {
        selectedCost.value = if (selectedCost.value == cost) null else cost
    }

    fun selectTrait(trait: String?) {
        selectedTrait.value = if (selectedTrait.value == trait) null else trait
    }

    fun toggleFavorite(championId: String) {
        viewModelScope.launch { favoritesRepository.toggle(FavoriteCategory.CHAMPION, championId) }
    }
}
