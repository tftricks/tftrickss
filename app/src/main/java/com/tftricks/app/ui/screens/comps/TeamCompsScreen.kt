package com.tftricks.app.ui.screens.comps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.Tier
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.FavoriteButton
import com.tftricks.app.ui.components.FilterChipRow
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.theme.TextSecondary

@Composable
fun TeamCompsScreen(
    contentPadding: PaddingValues,
    onOpenComp: (String) -> Unit,
    viewModel: TeamCompsViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
            item(key = "tier_filter") {
                FilterChipRow(
                    options = Tier.entries,
                    isSelected = { it == content.selectedTier },
                    onToggle = viewModel::selectTier,
                    label = { "${it.name} Tier" }
                )
            }
            item(key = "tag_filter") {
                FilterChipRow(
                    options = content.allTags,
                    isSelected = { it in content.selectedTags },
                    onToggle = viewModel::toggleTag,
                    label = { it }
                )
            }
            if (content.comps.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = "No comps match the selected filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
            items(content.comps, key = { it.id }) { comp ->
                TeamCompCard(
                    comp = comp,
                    isFavorite = comp.id in content.favoriteIds,
                    onClick = { onOpenComp(comp.id) },
                    onToggleFavorite = { viewModel.toggleFavorite(comp.id) }
                )
            }
        }
    }
}

@Composable
private fun TeamCompCard(
    comp: TeamComp,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    InfoCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TierBadge(comp.tier)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comp.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${comp.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }} • " +
                        "Carry: ${comp.carryChampions.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = comp.tags.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            FavoriteButton(isFavorite = isFavorite, onToggle = onToggleFavorite)
        }
    }
}
