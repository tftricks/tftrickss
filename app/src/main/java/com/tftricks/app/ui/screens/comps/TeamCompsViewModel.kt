package com.tftricks.app.ui.screens.comps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.Tier
import com.tftricks.app.domain.repository.FavoritesRepository
import com.tftricks.app.domain.repository.TeamCompRepository
import com.tftricks.app.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TeamCompsContent(
    val comps: List<TeamComp>,
    val allTags: List<String>,
    val selectedTier: Tier?,
    val selectedTags: Set<String>,
    val favoriteIds: Set<String>
)

class TeamCompsViewModel(
    private val repository: TeamCompRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val allComps = MutableStateFlow<List<TeamComp>>(emptyList())
    private val error = MutableStateFlow<String?>(null)
    private val loaded = MutableStateFlow(false)

    private val _selectedTier = MutableStateFlow<Tier?>(null)
    val selectedTier: StateFlow<Tier?> = _selectedTier.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    val uiState: StateFlow<UiState<TeamCompsContent>> = combine(
        allComps, loaded, error, _selectedTier, _selectedTags,
    ) { comps, isLoaded, err, tier, tags ->
        Triple(comps to isLoaded, err, tier to tags)
    }.combine(favoritesRepository.favorites(FavoriteCategory.COMP)) { (compsLoaded, err, filters), favorites ->
        val (comps, isLoaded) = compsLoaded
        val (tier, tags) = filters
        when {
            err != null -> UiState.Error(err)
            !isLoaded -> UiState.Loading
            else -> UiState.Success(
                TeamCompsContent(
                    comps = comps
                        .filter { tier == null || it.tier == tier }
                        .filter { tags.isEmpty() || it.tags.any { tag -> tag in tags } }
                        .sortedBy { it.tier },
                    allTags = comps.flatMap { it.tags }.distinct().sorted(),
                    selectedTier = tier,
                    selectedTags = tags,
                    favoriteIds = favorites
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            try {
                allComps.value = repository.getTeamComps()
                loaded.value = true
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load team comps"
            }
        }
    }

    fun selectTier(tier: Tier?) {
        _selectedTier.value = if (_selectedTier.value == tier) null else tier
    }

    fun toggleTag(tag: String) {
        _selectedTags.value =
            if (tag in _selectedTags.value) _selectedTags.value - tag
            else _selectedTags.value + tag
    }

    fun toggleFavorite(compId: String) {
        viewModelScope.launch { favoritesRepository.toggle(FavoriteCategory.COMP, compId) }
    }
}
