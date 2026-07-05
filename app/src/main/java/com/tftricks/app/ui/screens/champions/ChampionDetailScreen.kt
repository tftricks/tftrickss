package com.tftricks.app.ui.screens.champions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.FavoriteButton
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.PillChip
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChampionDetailScreen(
    contentPadding: PaddingValues,
    onOpenComp: (String) -> Unit,
    onOpenItem: (String) -> Unit,
    viewModel: ChampionDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { content ->
        val champion = content.champion

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${champion.cost}g",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = costColor(champion.cost)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = champion.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = champion.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    FavoriteButton(
                        isFavorite = content.isFavorite,
                        onToggle = viewModel::toggleFavorite
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    champion.traits.forEach { PillChip(text = it, contentColor = BrandYellow) }
                }
            }

            // Ability
            InfoCard {
                SectionLabel(text = "Ability")
                Text(
                    text = champion.ability.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = champion.ability.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (champion.ability.manaMax > 0) {
                    Text(
                        text = "Mana: ${champion.ability.manaStart} / ${champion.ability.manaMax}",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandYellow,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Recommended items
            InfoCard {
                SectionLabel(text = "Recommended items")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    champion.recommendedItems.forEach { itemName ->
                        PillChip(
                            text = itemName,
                            onClick = content.itemIdsByName[itemName]?.let { { onOpenItem(it) } }
                        )
                    }
                }
            }

            // Positioning
            InfoCard {
                SectionLabel(text = "Positioning")
                Text(
                    text = champion.positioningNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Best comps
            if (content.bestComps.isNotEmpty()) {
                SectionLabel(text = "Best comps", modifier = Modifier.padding(top = 4.dp))
                content.bestComps.forEach { comp ->
                    InfoCard(modifier = Modifier.clickable { onOpenComp(comp.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TierBadge(comp.tier)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = comp.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = comp.tags.joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
