package com.tftricks.app.ui.screens.champions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.BannerAdSlot
import com.tftricks.app.ui.components.FavoriteButton
import com.tftricks.app.ui.components.FilterChipRow
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

@Composable
fun ChampionsScreen(
    contentPadding: PaddingValues,
    onOpenChampion: (String) -> Unit,
    viewModel: ChampionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { content ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "cost_filter", span = { GridItemSpan(maxLineSpan) }) {
                    FilterChipRow(
                        options = (1..5).toList(),
                        isSelected = { it == content.selectedCost },
                        onToggle = viewModel::selectCost,
                        label = { "${it}g" }
                    )
                }
                item(key = "trait_filter", span = { GridItemSpan(maxLineSpan) }) {
                    FilterChipRow(
                        options = content.allTraits,
                        isSelected = { it == content.selectedTrait },
                        onToggle = viewModel::selectTrait,
                        label = { it }
                    )
                }
                if (content.champions.isEmpty()) {
                    item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "No champions match the selected filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
                items(content.champions, key = { it.id }) { champion ->
                    ChampionCard(
                        champion = champion,
                        isFavorite = champion.id in content.favoriteIds,
                        onClick = { onOpenChampion(champion.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(champion.id) }
                    )
                }
            }
            BannerAdSlot(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
        }
    }
}

@Composable
private fun ChampionCard(
    champion: Champion,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    InfoCard(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${champion.cost}g",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = costColor(champion.cost)
                    )
                    Text(
                        text = "  ${champion.name}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = champion.traits.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = champion.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            FavoriteButton(isFavorite = isFavorite, onToggle = onToggleFavorite)
        }
    }
}
