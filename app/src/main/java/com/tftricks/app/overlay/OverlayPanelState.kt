package com.tftricks.app.overlay

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tftricks.app.di.AppContainer
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.OverlaySession
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.repository.OverlaySessionRepository
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
 *
 * This instance lives for the whole service lifetime, so [selectedCompId] and the
 * scroll states below survive collapsing the panel to the bubble and expanding it
 * again unchanged — collapsing only unmounts the Compose UI, not this holder. They're
 * also snapshotted to DataStore via [persistSession] (call on collapse) so a service
 * restart restores the same screen. [OverlaySessionRepository.clear] wipes that saved
 * screen on a full stop — called directly by the service, not through this class, so
 * it can block until the write lands before the service's coroutine scope is cancelled.
 */
class OverlayPanelState(
    private val container: AppContainer,
    private val scope: CoroutineScope
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

    /** The comp shown in the center column, or null when the list hasn't been tapped yet. */
    var selectedCompId: String? by mutableStateOf(null)
    var searchQuery: String by mutableStateOf("")

    /** Scroll position of the center column's comp detail. */
    val centerScrollState = ScrollState(initial = 0)

    /** Scroll position of the right-hand comp list. */
    val rightListState = LazyListState()

    private val sessionRepository: OverlaySessionRepository = container.overlaySessionRepository

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
        scope.launch {
            runCatching { sessionRepository.load() }.getOrNull()?.let { session ->
                selectedCompId = session.selectedCompId
                centerScrollState.scrollTo(session.centerScrollOffset)
                rightListState.scrollToItem(session.listIndex, session.listOffset)
            }
        }
    }

    /** Select a comp from the list, resetting the center column's scroll to the top. */
    fun selectComp(compId: String) {
        selectedCompId = compId
        scope.launch { centerScrollState.scrollTo(0) }
    }

    /** Snapshot the current screen to DataStore. Call when the panel collapses. */
    fun persistSession() {
        val session = OverlaySession(
            selectedCompId = selectedCompId,
            centerScrollOffset = centerScrollState.value,
            listIndex = rightListState.firstVisibleItemIndex,
            listOffset = rightListState.firstVisibleItemScrollOffset
        )
        scope.launch { runCatching { sessionRepository.save(session) } }
    }
}
