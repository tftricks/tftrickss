package com.tftricks.app.overlay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.overlay.OverlayPanelState
import com.tftricks.app.ui.components.BoardCellData
import com.tftricks.app.ui.components.BoardGrid
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

/** Compact comp guide rendered inside the overlay: boards, items, positioning. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverlayCompDetail(
    comp: TeamComp,
    panelState: OverlayPanelState,
    compact: Boolean,
    onBack: () -> Unit
) {
    val champions by panelState.champions.collectAsState()
    val costByName = remember(champions) {
        champions.associate { it.name to it.cost }
    }
    val championColor = { name: String -> costColor(costByName[name] ?: 1) }

    val titleStyle =
        if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall
    val bodyStyle =
        if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to list",
                    tint = TextSecondary
                )
            }
            TierBadge(comp.tier)
            Spacer(modifier = Modifier.width(10.dp))
            Text(comp.name, style = titleStyle, color = TextPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)
        ) {
            SectionLabel(text = "Final board")
            val positioned = comp.finalBoard
                .filter { it.position != null }
                .associateBy { it.position!! }
            BoardGrid(
                cellFor = { position ->
                    positioned[position]?.let { unit ->
                        BoardCellData(
                            shortName = unit.champion,
                            accentColor = championColor(unit.champion)
                        )
                    }
                }
            )

            val itemized = comp.finalBoard.filter { it.items.isNotEmpty() }
            if (itemized.isNotEmpty()) {
                SectionLabel(text = "Items")
                itemized.forEach { unit ->
                    Text(
                        text = "${unit.champion}: ${unit.items.joinToString(", ")}",
                        style = bodyStyle,
                        color = TextSecondary
                    )
                }
            }
            Text(
                text = "Best: ${comp.bestItems.joinToString(", ")}",
                style = bodyStyle,
                color = TextPrimary
            )

            SectionLabel(text = "Early / mid game")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                comp.earlyGameBoard.forEach { unit ->
                    Text(unit.champion, style = bodyStyle, color = championColor(unit.champion))
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                comp.midGameBoard.forEach { unit ->
                    Text(unit.champion, style = bodyStyle, color = championColor(unit.champion))
                }
            }

            SectionLabel(text = "Positioning")
            Text(comp.positioningNotes, style = bodyStyle, color = TextSecondary)

            SectionLabel(text = "Roll timing")
            Text(comp.rollTiming, style = bodyStyle, color = TextSecondary)
        }
    }
}
