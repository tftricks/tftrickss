package com.tftricks.app.overlay

import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.Augment
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.SavedTeam
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.Trait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Data for the overlay panel. Deliberately not a ViewModel — the overlay lives in a
 * Service. Reads go through the same in-memory-cached repositories the app uses
 * (team comps bundled locally; champions/items/traits live from CommunityDragon), so
 * opening the overlay after browsing the app costs no extra fetch.
 */
class OverlayPanelState(
    container: AppContainer,
    scope: CoroutineScope
) {
    private val _comps = MutableStateFlow<List<TeamComp>>(emptyList())
    val comps: StateFlow<List<TeamComp>> = _comps.asStateFlow()

    private val _champions = MutableStateFlow<List<Champion>>(emptyList())
    val champions: StateFlow<List<Champion>> = _champions.asStateFlow()

    private val _traits = MutableStateFlow<List<Trait>>(emptyList())
    val traits: StateFlow<List<Trait>> = _traits.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _augments = MutableStateFlow<List<Augment>>(emptyList())
    val augments: StateFlow<List<Augment>> = _augments.asStateFlow()

    val favoriteCompIds: StateFlow<Set<String>> =
        container.favoritesRepository.favorites(FavoriteCategory.COMP)
            .stateIn(scope, SharingStarted.Eagerly, emptySet())

    val championIconUrls: StateFlow<Map<String, String>> = container.communityDragonRepository.championIconUrls
    val itemIconUrls: StateFlow<Map<String, String>> = container.communityDragonRepository.itemIconUrls

    val savedTeams: StateFlow<List<SavedTeam>> =
        container.savedTeamsRepository.savedTeams
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        scope.launch {
            runCatching {
                _comps.value = container.teamCompRepository.getTeamComps().sortedBy { it.tier }
                _champions.value = container.championRepository.getChampions()
                    .sortedWith(compareBy({ it.cost }, { it.name }))
                _traits.value = container.traitRepository.getTraits().sortedBy { it.name }
                _items.value = container.itemRepository.getItems().sortedBy { it.name }
                _augments.value = container.augmentRepository.getAugments()
                    .sortedByDescending { it.priorityRating }
            }
            // Champion/item/trait fetches can fail offline; comps stay bundled and unaffected.
        }
    }
}
