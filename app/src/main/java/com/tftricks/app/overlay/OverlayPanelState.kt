package com.tftricks.app.overlay

import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.TeamComp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data for the overlay panel. Deliberately not a ViewModel — the overlay lives in a
 * Service. Reads go through the same in-memory-cached repositories the app uses
 * (team comps bundled locally; champions/items live from CommunityDragon), so
 * opening the overlay after browsing the app costs no extra fetch. The overlay only
 * browses comps, so trait/augment/saved-team/favorite lists aren't loaded here.
 */
class OverlayPanelState(
    container: AppContainer,
    scope: CoroutineScope
) {
    private val _comps = MutableStateFlow<List<TeamComp>>(emptyList())
    val comps: StateFlow<List<TeamComp>> = _comps.asStateFlow()

    private val _champions = MutableStateFlow<List<Champion>>(emptyList())
    val champions: StateFlow<List<Champion>> = _champions.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    val championIconUrls: StateFlow<Map<String, String>> = container.communityDragonRepository.championIconUrls
    val itemIconUrls: StateFlow<Map<String, String>> = container.communityDragonRepository.itemIconUrls
    val traitIconUrls: StateFlow<Map<String, String>> = container.communityDragonRepository.traitIconUrls

    init {
        scope.launch {
            runCatching {
                _comps.value = container.teamCompRepository.getTeamComps().sortedBy { it.tier }
                _champions.value = container.championRepository.getChampions()
                    .sortedWith(compareBy({ it.cost }, { it.name }))
                _items.value = container.itemRepository.getItems().sortedBy { it.name }
            }
            // Champion/item fetches can fail offline; comps stay bundled and unaffected.
        }
    }
}
