package com.tftricks.app.ui.screens.augments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.Augment
import com.tftricks.app.domain.model.AugmentTier
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.repository.AugmentRepository
import com.tftricks.app.domain.repository.FavoritesRepository
import com.tftricks.app.domain.repository.TeamCompRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AugmentsContent(
    val augments: List<Augment>,
    val selectedTier: AugmentTier?,
    val favoriteIds: Set<String>,
    /** Comp name lookup by id, to render tappable best-comp chips. */
    val compNamesById: Map<String, String>
)

class AugmentsViewModel(
    private val repository: AugmentRepository,
    private val teamCompRepository: TeamCompRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val loaded = MutableStateFlow<Pair<List<Augment>, Map<String, String>>?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val selectedTier = MutableStateFlow<AugmentTier?>(null)

    val uiState: StateFlow<UiState<AugmentsContent>> = combine(
        loaded, error, selectedTier,
        favoritesRepository.favorites(FavoriteCategory.AUGMENT)
    ) { data, err, tier, favorites ->
        when {
            err != null -> UiState.Error(err)
            data == null -> UiState.Loading
            else -> UiState.Success(
                AugmentsContent(
                    augments = data.first
                        .filter { tier == null || it.tier == tier }
                        .sortedWith(compareByDescending<Augment> { it.priorityRating }.thenBy { it.name }),
                    selectedTier = tier,
                    favoriteIds = favorites,
                    compNamesById = data.second
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            try {
                loaded.value = repository.getAugments() to
                    teamCompRepository.getTeamComps().associate { it.id to it.name }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load augments"
            }
        }
    }

    fun selectTier(tier: AugmentTier?) {
        selectedTier.value = if (selectedTier.value == tier) null else tier
    }

    fun toggleFavorite(augmentId: String) {
        viewModelScope.launch { favoritesRepository.toggle(FavoriteCategory.AUGMENT, augmentId) }
    }
}
