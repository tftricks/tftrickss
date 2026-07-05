package com.tftricks.app.ui.screens.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.SavedTeam
import com.tftricks.app.domain.model.SavedUnit
import com.tftricks.app.domain.model.Trait
import com.tftricks.app.domain.repository.ChampionRepository
import com.tftricks.app.domain.repository.SavedTeamsRepository
import com.tftricks.app.domain.repository.TraitRepository
import com.tftricks.app.ui.common.UiState
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** How close a trait is to (or past) its breakpoints for the current board. */
data class TraitStatus(
    val trait: Trait,
    /** Unique board units carrying this trait. */
    val count: Int,
    /** Index into trait.breakpoints of the highest breakpoint reached, or null if inactive. */
    val activeBreakpointIndex: Int?
) {
    val isActive: Boolean get() = activeBreakpointIndex != null
}

data class TeamBuilderContent(
    /** Full roster, sorted by cost then name. */
    val roster: List<Champion>,
    /** Occupied board cells: position → champion. */
    val board: Map<Int, Champion>,
    val selectedCell: Int?,
    /** Traits with at least one unit on the board, active first. */
    val traitStatuses: List<TraitStatus>,
    /** Set after a successful save; cleared on the next board edit. */
    val savedMessage: String?
)

class TeamBuilderViewModel(
    private val championRepository: ChampionRepository,
    private val traitRepository: TraitRepository,
    private val savedTeamsRepository: SavedTeamsRepository
) : ViewModel() {

    private val data = MutableStateFlow<Pair<List<Champion>, List<Trait>>?>(null)
    private val error = MutableStateFlow<String?>(null)

    private val board = MutableStateFlow<Map<Int, String>>(emptyMap())
    private val selectedCell = MutableStateFlow<Int?>(null)
    private val savedMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState<TeamBuilderContent>> = combine(
        data, error, board, selectedCell, savedMessage
    ) { loaded, err, boardIds, selected, saved ->
        when {
            err != null -> UiState.Error(err)
            loaded == null -> UiState.Loading
            else -> {
                val (champions, traits) = loaded
                val byId = champions.associateBy { it.id }
                val boardChampions = boardIds.mapNotNull { (position, id) ->
                    byId[id]?.let { position to it }
                }.toMap()
                UiState.Success(
                    TeamBuilderContent(
                        roster = champions.sortedWith(compareBy({ it.cost }, { it.name })),
                        board = boardChampions,
                        selectedCell = selected,
                        traitStatuses = computeTraits(boardChampions.values.distinctBy { it.id }, traits),
                        savedMessage = saved
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        viewModelScope.launch {
            try {
                data.value = championRepository.getChampions() to traitRepository.getTraits()
            } catch (e: Exception) {
                error.value = e.message ?: "Failed to load builder data"
            }
        }
    }

    private fun computeTraits(units: Collection<Champion>, traits: List<Trait>): List<TraitStatus> {
        val counts = units.flatMap { it.traits }.groupingBy { it }.eachCount()
        return traits.mapNotNull { trait ->
            val count = counts[trait.name] ?: return@mapNotNull null
            val activeIndex = trait.breakpoints.indexOfLast { it.count <= count }
            TraitStatus(
                trait = trait,
                count = count,
                activeBreakpointIndex = activeIndex.takeIf { it >= 0 }
            )
        }.sortedWith(
            compareByDescending<TraitStatus> { it.isActive }
                .thenByDescending { it.count }
                .thenBy { it.trait.name }
        )
    }

    /** Tap a board cell: remove its unit if occupied, otherwise select it as placement target. */
    fun onCellClick(position: Int) {
        savedMessage.value = null
        if (position in board.value) {
            board.value = board.value - position
            selectedCell.value = position
        } else {
            selectedCell.value = if (selectedCell.value == position) null else position
        }
    }

    /** Tap a roster champion: toggle it off the board, or place it on the selected/first free cell. */
    fun onChampionClick(champion: Champion) {
        savedMessage.value = null
        val current = board.value
        val existing = current.entries.find { it.value == champion.id }
        if (existing != null) {
            board.value = current - existing.key
            return
        }
        val target = selectedCell.value
            ?: (0 until 28).firstOrNull { it !in current }
            ?: return
        board.value = current + (target to champion.id)
        selectedCell.value = null
    }

    fun clearBoard() {
        savedMessage.value = null
        board.value = emptyMap()
        selectedCell.value = null
    }

    fun saveTeam(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || board.value.isEmpty()) return
        viewModelScope.launch {
            savedTeamsRepository.saveTeam(
                SavedTeam(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    units = board.value.map { (position, id) -> SavedUnit(position, id) }
                        .sortedBy { it.position },
                    createdAt = System.currentTimeMillis()
                )
            )
            savedMessage.value = "Saved \"$trimmed\" to Saved Comps"
        }
    }
}
