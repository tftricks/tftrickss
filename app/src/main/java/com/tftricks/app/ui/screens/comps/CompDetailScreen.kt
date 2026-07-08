package com.tftricks.app.ui.screens.comps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tftricks.app.BuildConfig
import com.tftricks.app.TFTricksApplication
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.domain.model.BoardUnit
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.BoardCellData
import com.tftricks.app.ui.components.BoardGrid
import com.tftricks.app.ui.components.DataDragonDebugBanner
import com.tftricks.app.ui.components.ExpandableSection
import com.tftricks.app.ui.components.FavoriteButton
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.PillChip
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.components.rememberDataDragonRepository
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.DangerRed
import com.tftricks.app.ui.theme.SuccessGreen
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompDetailScreen(
    contentPadding: PaddingValues,
    onOpenChampion: (String) -> Unit,
    onOpenItem: (String) -> Unit,
    viewModel: CompDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dataDragon = rememberDataDragonRepository()
    val championIcons by dataDragon.championIconUrls.collectAsStateWithLifecycle()
    val dataDragonStatus by dataDragon.status.collectAsStateWithLifecycle()
    var debugBannerDismissed by remember { mutableStateOf(false) }

    // Interstitial hook (no-op unless AdsConfig.INTERSTITIALS_ENABLED).
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        (context.applicationContext as TFTricksApplication).container.adsManager
            .onCompDetailOpened(context as? Activity)
    }

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { content ->
        val comp = content.comp
        val championColor = { name: String ->
            costColor(content.championsByName[name]?.cost ?: 1)
        }
        val championChip: @Composable (String) -> Unit = { name ->
            PillChip(
                text = name,
                contentColor = championColor(name),
                onClick = content.championsByName[name]?.let { { onOpenChampion(it.id) } }
            )
        }
        val itemChip: @Composable (String) -> Unit = { name ->
            PillChip(
                text = name,
                onClick = content.itemIdsByName[name]?.let { { onOpenItem(it) } }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (BuildConfig.DEBUG && !debugBannerDismissed) {
                DataDragonDebugBanner(
                    status = dataDragonStatus,
                    onDismiss = { debugBannerDismissed = true }
                )
            }

            // Header
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TierBadge(comp.tier)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = comp.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${comp.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }} • " +
                                "Patch ${comp.patchVersion}",
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
                    comp.tags.forEach { PillChip(text = it, contentColor = BrandYellow) }
                }
            }

            ExpandableSection(title = "Final Board", initiallyExpanded = true) {
                val positioned = comp.finalBoard
                    .filter { it.position != null }
                    .associateBy { it.position!! }
                BoardGrid(
                    cellFor = { position ->
                        positioned[position]?.let { unit ->
                            BoardCellData(
                                shortName = unit.champion,
                                accentColor = championColor(unit.champion),
                                iconUrl = championIcons[unit.champion]
                            )
                        }
                    }
                )
                val flexUnits = comp.finalBoard.filter { it.position == null }
                if (flexUnits.isNotEmpty()) {
                    SectionLabel(text = "Flexible position", modifier = Modifier.padding(top = 8.dp))
                    UnitChipRow(flexUnits, championChip)
                }
                SectionLabel(text = "Itemized units", modifier = Modifier.padding(top = 8.dp))
                comp.finalBoard.filter { it.items.isNotEmpty() }.forEach { unit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        championChip(unit.champion)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = unit.items.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            ExpandableSection(title = "Early & Mid Game") {
                SectionLabel(text = "Early game")
                UnitChipRow(comp.earlyGameBoard, championChip)
                SectionLabel(text = "Mid game", modifier = Modifier.padding(top = 10.dp))
                UnitChipRow(comp.midGameBoard, championChip)
            }

            ExpandableSection(title = "Carries & Tanks", initiallyExpanded = true) {
                SectionLabel(text = "Carries")
                ChipFlow(comp.carryChampions, championChip)
                SectionLabel(text = "Tanks", modifier = Modifier.padding(top = 10.dp))
                ChipFlow(comp.tankChampions, championChip)
            }

            ExpandableSection(title = "Items") {
                SectionLabel(text = "Best items")
                ChipFlow(comp.bestItems, itemChip)
                if (comp.alternativeItems.isNotEmpty()) {
                    SectionLabel(text = "Alternatives", modifier = Modifier.padding(top = 10.dp))
                    ChipFlow(comp.alternativeItems, itemChip)
                }
            }

            ExpandableSection(title = "Active Traits") {
                comp.traitsActive.forEach { active ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = active.count.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = BrandYellow
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = active.trait,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }

            ExpandableSection(title = "Game Plan") {
                LabeledParagraph("Positioning", comp.positioningNotes)
                LabeledParagraph("Leveling", comp.levelingGuide)
                LabeledParagraph("Economy", comp.economyGuide)
                LabeledParagraph("Roll timing", comp.rollTiming)
                LabeledParagraph("When to play", comp.whenToPlay)
            }

            ExpandableSection(title = "Strengths & Weaknesses") {
                comp.strengths.forEach {
                    BulletLine(text = it, bullet = "+", color = SuccessGreen)
                }
                Spacer(modifier = Modifier.height(6.dp))
                comp.weaknesses.forEach {
                    BulletLine(text = it, bullet = "–", color = DangerRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(names: List<String>, chip: @Composable (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        names.forEach { chip(it) }
    }
}

@Composable
private fun UnitChipRow(units: List<BoardUnit>, chip: @Composable (String) -> Unit) {
    ChipFlow(units.map { it.champion }, chip)
}

@Composable
private fun LabeledParagraph(label: String, text: String) {
    SectionLabel(text = label, modifier = Modifier.padding(top = 8.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun BulletLine(text: String, bullet: String, color: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = bullet,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
