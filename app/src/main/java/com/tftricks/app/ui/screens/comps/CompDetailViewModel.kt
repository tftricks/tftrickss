package com.tftricks.app.ui.screens.comps

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.ComponentRequirement
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.requiredComponents
import com.tftricks.app.ui.common.UiState
import com.tftricks.app.ui.navigation.DetailRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CompDetailContent(
    val comp: TeamComp,
    val isFavorite: Boolean,
    /** Champion lookup by display name, for cost colors and navigation from boards. */
    val championsByName: Map<String, Champion>,
    /** Item id lookup by display name, for navigation from item chips. */
    val itemIdsByName: Map<String, String>,
    /** Base components needed for this comp's full itemization, tallied from live recipe data. */
    val requiredComponents: List<ComponentRequirement>
)

private data class LoadedCompDetail(
    val comp: TeamComp,
    val championsByName: Map<String, Champion>,
    val itemIdsByName: Map<String, String>,
    val requiredComponents: List<ComponentRequirement>
)

class CompDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer
) : ViewModel() {

    private val compId: String = checkNotNull(savedStateHandle[DetailRoutes.COMP_ARG])

    private val data = MutableStateFlow<LoadedCompDetail?>(null)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<CompDetailContent>> = combine(
        data, error, container.favoritesRepository.favorites(FavoriteCategory.COMP)
    ) { loaded, err, favorites ->
        when {
            err != null -> UiState.Error(err)
            loaded == null -> UiState.Loading
            else -> UiState.Success(
                CompDetailContent(
                    comp = loaded.comp,
                    isFavorite = compId in favorites,
                    championsByName = loaded.championsByName,
                    itemIdsByName = loaded.itemIdsByName,
                    requiredComponents = loaded.requiredComponents
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            try {
                val comp = container.teamCompRepository.getTeamComp(compId)
                if (comp == null) {
                    error.value = "Comp not found"
                } else {
                    // This comp guide is bundled locally, so it shouldn't be blocked by a
                    // failed live champion/item fetch — fall back to empty lookups instead.
                    val champions = runCatching { container.championRepository.getChampions() }
                        .getOrDefault(emptyList())
                    val items = runCatching { container.itemRepository.getItems() }
                        .getOrDefault(emptyList())
                    data.value = LoadedCompDetail(
                        comp = comp,
                        championsByName = champions.associateBy { it.name },
                        itemIdsByName = items.associate { it.name to it.id },
                        requiredComponents = comp.requiredComponents(items.associateBy { it.name })
                    )
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load comp"
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            container.favoritesRepository.toggle(FavoriteCategory.COMP, compId)
        }
    }
}
