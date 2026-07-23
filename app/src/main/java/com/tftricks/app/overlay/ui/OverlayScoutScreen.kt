package com.tftricks.app.overlay.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.tftricks.app.domain.model.PivotRecommendation
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.Tier
import com.tftricks.app.domain.model.recommendPivots
import com.tftricks.app.overlay.OverlayPanelState
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.PillChip
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.SurfaceElevated
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary

/**
 * Scout tab: assign one of our S-tier comps (or "Unknown") to each of the 7 opponent
 * slots, see the live read on the lobby, and get pivot suggestions ranked by how little
 * they overlap the comp(s) most of the lobby is on. Assignments live in
 * [OverlayPanelState.scoutAssignments] so they survive collapse/expand; [Reset] clears
 * them for a new game.
 */
@Composable
fun OverlayScoutScreen(
    panelState: OverlayPanelState,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val comps by panelState.comps.collectAsState()
    val sTierComps = remember(comps) { comps.filter { it.tier == Tier.S } }
    val compsById = remember(comps) { comps.associateBy { it.id } }
    val assignments = panelState.scoutAssignments

    val titleStyle =
        if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val bodyStyle =
        if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Scout",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { panelState.resetScout() }) {
                Text("Reset", style = bodyStyle, color = BrandYellow)
            }
        }

        SectionLabel(text = "Lobby read")
        Text(text = scoutTally(assignments, compsById), style = bodyStyle, color = TextSecondary)

        SectionLabel(text = "Opponents")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (slot in assignments.indices) {
                OverlayScoutSlot(
                    slotNumber = slot + 1,
                    assignedCompId = assignments[slot],
                    options = sTierComps,
                    titleStyle = titleStyle,
                    bodyStyle = bodyStyle,
                    onAssign = { compId -> panelState.assignScout(slot, compId) }
                )
            }
        }

        SectionLabel(text = "Recommended pivot")
        val recommendations = recommendPivots(comps, assignments)
        if (recommendations.isEmpty()) {
            Text(
                text = "Scout at least one opponent to see pivot recommendations.",
                style = bodyStyle,
                color = TextSecondary
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommendations.forEach { recommendation ->
                    OverlayPivotCard(recommendation, titleStyle, bodyStyle)
                }
            }
        }
    }
}

@Composable
private fun OverlayScoutSlot(
    slotNumber: Int,
    assignedCompId: String?,
    options: List<TeamComp>,
    titleStyle: TextStyle,
    bodyStyle: TextStyle,
    onAssign: (String?) -> Unit
) {
    Column {
        Text(text = "Opponent $slotNumber", style = titleStyle, color = TextPrimary)
        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { comp ->
                val selected = assignedCompId == comp.id
                PillChip(
                    text = comp.name,
                    contentColor = if (selected) PureBlack else TextPrimary,
                    containerColor = if (selected) BrandYellow else SurfaceElevated,
                    onClick = { onAssign(comp.id) }
                )
            }
            val unknownSelected = assignedCompId == null
            PillChip(
                text = "Unknown",
                contentColor = if (unknownSelected) PureBlack else TextSecondary,
                containerColor = if (unknownSelected) BrandYellow else SurfaceElevated,
                onClick = { onAssign(null) }
            )
        }
    }
}

@Composable
private fun OverlayPivotCard(
    recommendation: PivotRecommendation,
    titleStyle: TextStyle,
    bodyStyle: TextStyle
) {
    InfoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TierBadge(recommendation.comp.tier)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = recommendation.comp.name,
                style = titleStyle,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "overlap ${recommendation.overlapScore}",
                style = bodyStyle,
                color = TextSecondary
            )
        }
        Text(
            text = recommendation.reason,
            style = bodyStyle,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** e.g. "Meep Show 4/7 · Disco Inferno 2/7 · Unknown 1/7", or a placeholder when empty. */
private fun scoutTally(assignments: List<String?>, compsById: Map<String, TeamComp>): String {
    val total = assignments.size
    val counts = assignments.groupingBy { it }.eachCount()
    val known = counts.entries.filter { it.key != null }.sortedByDescending { it.value }
    if (known.isEmpty()) return "No opponents scouted yet"
    val parts = known.map { (id, count) -> "${compsById[id]?.name ?: "Unknown comp"} $count/$total" }.toMutableList()
    val unknownCount = counts[null] ?: 0
    if (unknownCount > 0) parts.add("Unknown $unknownCount/$total")
    return parts.joinToString(" · ")
}
