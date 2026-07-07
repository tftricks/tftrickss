package com.tftricks.app.ui.screens.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SavedCompsScreen(
    contentPadding: PaddingValues,
    onOpenComp: (String) -> Unit,
    onOpenBuilder: () -> Unit,
    viewModel: SavedCompsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { content ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "fav_header") { SectionLabel(text = "Favorite comps") }
            if (content.favoriteComps.isEmpty()) {
                item(key = "fav_empty") {
                    Text(
                        text = "No favorite comps yet. Tap the heart on any comp to save it here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            items(content.favoriteComps, key = { "fav_${it.id}" }) { comp ->
                InfoCard(onClick = { onOpenComp(comp.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TierBadge(comp.tier)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
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
                        FavoriteButton(
                            isFavorite = true,
                            onToggle = { viewModel.unfavorite(comp.id) }
                        )
                    }
                }
            }

            item(key = "teams_header") {
                SectionLabel(text = "My teams", modifier = Modifier.padding(top = 8.dp))
            }
            if (content.savedTeams.isEmpty()) {
                item(key = "teams_empty") {
                    InfoCard(onClick = onOpenBuilder) {
                        Text(
                            text = "No saved teams yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Open the Team Builder →",
                            style = MaterialTheme.typography.titleSmall,
                            color = BrandYellow,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            items(content.savedTeams, key = { "team_${it.id}" }) { team ->
                InfoCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.deleteTeam(team.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete team",
                                tint = TextSecondary
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        team.units.sortedBy { it.position }.forEach { unit ->
                            val champion = content.championsById[unit.championId]
                            PillChip(
                                text = champion?.name ?: unit.championId,
                                contentColor = costColor(champion?.cost ?: 1)
                            )
                        }
                    }
                }
            }
        }
    }
}
