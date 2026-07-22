package com.tftricks.app.overlay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.requiredComponents
import com.tftricks.app.overlay.OverlayPanelState
import com.tftricks.app.ui.components.BoardCellData
import com.tftricks.app.ui.components.BoardGrid
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

/** Compact comp guide rendered in the overlay's center column: board, items, game plan. */
@Composable
fun OverlayCompDetail(
    comp: TeamComp,
    panelState: OverlayPanelState,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val champions by panelState.champions.collectAsState()
    val championIcons by panelState.championIconUrls.collectAsState()
    val items by panelState.items.collectAsState()
    val itemIcons by panelState.itemIconUrls.collectAsState()
    val costByName = remember(champions) { champions.associate { it.name to it.cost } }
    val itemIconUrlByName = remember(items, itemIcons) {
        items.mapNotNull { i -> itemIcons[i.id]?.let { i.name to it } }.toMap()
    }
    val requiredComponents = remember(comp, items) {
        comp.requiredComponents(items.associateBy { it.name })
    }
    val championColor = { name: String -> costColor(costByName[name] ?: 1) }

    val titleStyle =
        if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall
    val bodyStyle =
        if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TierBadge(comp.tier)
            Spacer(modifier = Modifier.width(10.dp))
            Text(comp.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }

        val positioned = comp.finalBoard
            .filter { it.position != null }
            .associateBy { it.position!! }
        BoardGrid(
            cellFor = { position ->
                positioned[position]?.let { unit ->
                    BoardCellData(
                        shortName = unit.champion,
                        accentColor = championColor(unit.champion),
                        iconUrl = championIcons[unit.champion],
                        itemIconUrls = unit.items.mapNotNull { itemIconUrlByName[it] },
                        starTarget = unit.starTarget
                    )
                }
            }
        )

        val carriesWithItems = comp.finalBoard.filter { it.champion in comp.carryChampions && it.items.isNotEmpty() }
        if (carriesWithItems.isNotEmpty()) {
            SectionLabel(text = "Items per carry")
            carriesWithItems.forEach { unit ->
                Text(
                    text = "${unit.champion}: ${unit.items.joinToString(", ")}",
                    style = bodyStyle,
                    color = TextSecondary
                )
            }
        }

        SectionLabel(text = "Game plan")
        LabeledLine("Positioning", comp.positioningNotes, bodyStyle)
        LabeledLine("Leveling curve", comp.levelingGuide, bodyStyle)
        LabeledLine("Economy", comp.economyGuide, bodyStyle)
        LabeledLine("Roll timing", comp.rollTiming, bodyStyle)
        LabeledLine("When to play", comp.whenToPlay, bodyStyle)

        if (comp.levelingStages.isNotEmpty()) {
            SectionLabel(text = "Leveling")
            comp.levelingStages.forEach { stage ->
                Text(text = "• $stage", style = bodyStyle, color = TextSecondary)
            }
        }

        if (comp.earlyGameNotes.isNotEmpty()) {
            SectionLabel(text = "Early game")
            comp.earlyGameNotes.forEach { note ->
                Text(text = "• $note", style = bodyStyle, color = TextSecondary)
            }
        }

        if (comp.augmentGuide != null) {
            SectionLabel(text = "Augments")
            OverlayAugmentTier("1st", comp.augmentGuide.tier1, bodyStyle)
            OverlayAugmentTier("2nd", comp.augmentGuide.tier2, bodyStyle)
            OverlayAugmentTier("3rd", comp.augmentGuide.tier3, bodyStyle)
        }

        if (comp.carouselItemPriority.isNotEmpty()) {
            SectionLabel(text = "Carousel priority")
            Text(
                text = comp.carouselItemPriority.joinToString(" > "),
                style = bodyStyle,
                color = TextSecondary
            )
        }

        if (requiredComponents.isNotEmpty()) {
            SectionLabel(text = "Component items required")
            Text(
                text = requiredComponents.joinToString(", ") { "${it.componentName} ×${it.count}" },
                style = bodyStyle,
                color = TextSecondary
            )
        }

        if (comp.tips.isNotEmpty()) {
            SectionLabel(text = "Tips")
            comp.tips.forEach { tip ->
                Text(text = "• $tip", style = bodyStyle, color = TextSecondary)
            }
        }

        if (comp.variants.isNotEmpty()) {
            SectionLabel(text = "Variants")
            comp.variants.forEach { variant ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    TierBadge(variant.tier)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(variant.name, style = titleStyle, color = BrandYellow)
                }
                if (variant.notes.isNotBlank()) {
                    Text(variant.notes, style = bodyStyle, color = TextSecondary)
                }
                if (variant.statsNote.isNotBlank()) {
                    Text(variant.statsNote, style = bodyStyle, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun OverlayAugmentTier(label: String, augments: List<String>, bodyStyle: TextStyle) {
    if (augments.isEmpty()) return
    Text(
        text = "$label: ${augments.joinToString(", ")}",
        style = bodyStyle,
        color = TextSecondary
    )
}

@Composable
private fun LabeledLine(label: String, text: String, bodyStyle: TextStyle) {
    if (text.isBlank()) return
    Text(
        text = "$label: $text",
        style = bodyStyle,
        color = TextSecondary,
        modifier = Modifier.fillMaxWidth()
    )
}
