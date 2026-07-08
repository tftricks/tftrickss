package com.tftricks.app.ui.screens.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.tftricks.app.ui.components.GameIcon
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.PillChip
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.components.rememberDataDragonRepository
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemDetailScreen(
    contentPadding: PaddingValues,
    onOpenChampion: (String) -> Unit,
    onOpenItem: (String) -> Unit,
    viewModel: ItemDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dataDragon = rememberDataDragonRepository()
    val itemIcons by dataDragon.itemIconUrls.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { content ->
        val item = content.item

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GameIcon(url = itemIcons[item.id], modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = item.category.displayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandYellow
                        )
                    }
                    FavoriteButton(
                        isFavorite = content.isFavorite,
                        onToggle = viewModel::toggleFavorite
                    )
                }
            }

            InfoCard {
                SectionLabel(text = "Recipe")
                Text(
                    text = if (item.components.isEmpty()) "Base component — no recipe"
                    else item.components.joinToString("  +  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            InfoCard {
                SectionLabel(text = "Effect")
                Text(
                    text = item.effect,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (item.bestUsers.isNotEmpty()) {
                InfoCard {
                    SectionLabel(text = "Best users")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        item.bestUsers.forEach { name ->
                            PillChip(
                                text = name,
                                onClick = content.championIdsByName[name]?.let { { onOpenChampion(it) } }
                            )
                        }
                    }
                }
            }

            if (item.goodAlternatives.isNotEmpty()) {
                InfoCard {
                    SectionLabel(text = "Good alternatives")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        item.goodAlternatives.forEach { name ->
                            PillChip(
                                text = name,
                                onClick = content.itemIdsByName[name]?.let { { onOpenItem(it) } }
                            )
                        }
                    }
                }
            }
        }
    }
}
