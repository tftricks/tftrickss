package com.tftricks.app.ui.screens.home

import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.overlay.OverlayService
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.BannerAdSlot
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.PillChip
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.components.TFTricksLogo
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.navigation.Destination
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.SurfaceElevated
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onOpenDestination: (Destination) -> Unit,
    onOpenComp: (String) -> Unit,
    onOpenChampion: (String) -> Unit,
    onOpenItem: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding), onRetry = viewModel::retry) { content ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            // Branding + patch
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                TFTricksLogo(markSize = 64.dp)
                Text(
                    text = "Patch ${content.currentPatch}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Quick access
            SectionLabel(text = "Quick access")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickTile(Destination.TeamComps, "Meta Comps", onOpenDestination, Modifier.weight(1f))
                QuickTile(Destination.Champions, "Champions", onOpenDestination, Modifier.weight(1f))
                QuickTile(Destination.Items, "Items", onOpenDestination, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickTile(Destination.Traits, "Traits", onOpenDestination, Modifier.weight(1f))
                QuickTile(Destination.Augments, "Augments", onOpenDestination, Modifier.weight(1f))
                QuickTile(Destination.TeamBuilder, "Builder", onOpenDestination, Modifier.weight(1f))
            }

            // In-game overlay quick toggle
            OverlayToggleCard(onOpenDestination)

            // Featured S-tier comps
            SectionLabel(text = "Featured comps", modifier = Modifier.padding(top = 4.dp))
            content.featuredComps.forEach { comp ->
                InfoCard(onClick = { onOpenComp(comp.id) }) {
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

            // Favorites
            val hasFavorites = content.favoriteComps.isNotEmpty() ||
                content.favoriteChampions.isNotEmpty() ||
                content.favoriteItems.isNotEmpty()
            if (hasFavorites) {
                SectionLabel(text = "Favorites", modifier = Modifier.padding(top = 4.dp))
                content.favoriteComps.forEach { comp ->
                    InfoCard(onClick = { onOpenComp(comp.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TierBadge(comp.tier)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = comp.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
                if (content.favoriteChampions.isNotEmpty() || content.favoriteItems.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        content.favoriteChampions.forEach { champion ->
                            PillChip(
                                text = champion.name,
                                contentColor = costColor(champion.cost),
                                onClick = { onOpenChampion(champion.id) }
                            )
                        }
                        content.favoriteItems.forEach { item ->
                            PillChip(
                                text = item.name,
                                onClick = { onOpenItem(item.id) }
                            )
                        }
                    }
                }
            }
            }
            BannerAdSlot(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
        }
    }
}

@Composable
private fun OverlayToggleCard(onOpenDestination: (Destination) -> Unit) {
    val context = LocalContext.current
    val isRunning by OverlayService.isRunning.collectAsStateWithLifecycle()

    InfoCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "In-game overlay",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = if (isRunning) "Floating button is on screen."
                    else "Show comps and items on top of TFT.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = isRunning,
                onCheckedChange = { wantOn ->
                    when {
                        !wantOn -> OverlayService.stop(context)
                        Settings.canDrawOverlays(context) -> OverlayService.start(context)
                        // No permission yet → walk the user through it in Overlay Settings.
                        else -> onOpenDestination(Destination.OverlaySettings)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureBlack,
                    checkedTrackColor = BrandYellow,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = SurfaceElevated
                )
            )
        }
    }
}

@Composable
private fun QuickTile(
    destination: Destination,
    label: String,
    onOpen: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    InfoCard(modifier = modifier, onClick = { onOpen(destination) }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                tint = BrandYellow
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp),
                maxLines = 1
            )
        }
    }
}
