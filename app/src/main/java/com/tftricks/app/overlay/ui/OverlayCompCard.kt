package com.tftricks.app.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.repository.TeamPlannerRepository
import com.tftricks.app.ui.components.CopyCompButton
import com.tftricks.app.ui.components.GameIcon
import com.tftricks.app.ui.components.HexagonShape
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.components.UnitIconStack
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.SurfaceCard
import com.tftricks.app.ui.theme.SurfaceElevated
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

/**
 * A comp card for the overlay's right-hand list: tier badge, name, trait icons in
 * hexes, a one-word playstyle label, and a roster row (champion icons with mini item
 * icons and 3★ badges), Lolchess-style.
 */
@Composable
fun OverlayCompCard(
    comp: TeamComp,
    isSelected: Boolean,
    championIcons: Map<String, String>,
    itemIconUrlsByName: Map<String, String>,
    traitIcons: Map<String, String>,
    championsByName: Map<String, Champion>,
    teamPlannerRepository: TeamPlannerRepository,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) SurfaceElevated else SurfaceCard, shape)
            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) BrandYellow else OutlineDark, shape)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TierBadge(comp.tier)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comp.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = comp.tags.firstOrNull() ?: comp.difficulty.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            if (comp.traitsActive.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    comp.traitsActive.take(4).forEach { active ->
                        GameIcon(
                            url = traitIcons[active.trait],
                            shape = HexagonShape,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            CopyCompButton(
                comp = comp,
                teamPlannerRepository = teamPlannerRepository,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            comp.finalBoard
                .sortedBy { it.position ?: Int.MAX_VALUE }
                .forEach { unit ->
                    val champion = championsByName[unit.champion]
                    UnitIconStack(
                        championIconUrl = championIcons[unit.champion],
                        accentColor = if (champion != null) costColor(champion.cost) else OutlineDark,
                        starTarget = unit.starTarget,
                        itemIconUrls = unit.items.mapNotNull { itemIconUrlsByName[it] },
                        iconSize = 26.dp
                    )
                }
        }
    }
}
