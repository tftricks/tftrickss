package com.tftricks.app.ui.screens.augments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.domain.model.Augment
import com.tftricks.app.domain.model.AugmentTier
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.FavoriteButton
import com.tftricks.app.ui.components.FilterChipRow
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.PillChip
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.theme.AugmentGold
import com.tftricks.app.ui.theme.AugmentPrismatic
import com.tftricks.app.ui.theme.AugmentSilver
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.TextSecondary

fun AugmentTier.displayName(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

fun AugmentTier.color(): Color = when (this) {
    AugmentTier.SILVER -> AugmentSilver
    AugmentTier.GOLD -> AugmentGold
    AugmentTier.PRISMATIC -> AugmentPrismatic
}

@Composable
fun AugmentsScreen(
    contentPadding: PaddingValues,
    onOpenComp: (String) -> Unit,
    viewModel: AugmentsViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
                    options = AugmentTier.entries,
                    isSelected = { it == content.selectedTier },
                    onToggle = viewModel::selectTier,
                    label = { it.displayName() }
                )
            }
            if (content.augments.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = "No augments in this tier.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
            items(content.augments, key = { it.id }) { augment ->
                AugmentCard(
                    augment = augment,
                    isFavorite = augment.id in content.favoriteIds,
                    compNamesById = content.compNamesById,
                    onOpenComp = onOpenComp,
                    onToggleFavorite = { viewModel.toggleFavorite(augment.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AugmentCard(
    augment: Augment,
    isFavorite: Boolean,
    compNamesById: Map<String, String>,
    onOpenComp: (String) -> Unit,
    onToggleFavorite: () -> Unit
) {
    InfoCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = augment.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = augment.tier.color()
                )
                Text(
                    text = "${augment.tier.displayName()} • Priority " +
                        "★".repeat(augment.priorityRating) +
                        "☆".repeat(5 - augment.priorityRating),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            FavoriteButton(isFavorite = isFavorite, onToggle = onToggleFavorite)
        }
        Text(
            text = augment.effect,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (augment.notes.isNotBlank()) {
            Text(
                text = augment.notes,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        val comps = augment.bestComps.mapNotNull { id ->
            compNamesById[id]?.let { name -> id to name }
        }
        if (comps.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                comps.forEach { (id, name) ->
                    PillChip(
                        text = name,
                        contentColor = BrandYellow,
                        onClick = { onOpenComp(id) }
                    )
                }
            }
        }
    }
}
