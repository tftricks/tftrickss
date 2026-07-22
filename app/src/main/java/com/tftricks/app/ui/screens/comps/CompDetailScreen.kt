package com.tftricks.app.ui.screens.comps

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tftricks.app.BuildConfig
import com.tftricks.app.TFTricksApplication
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.domain.model.BoardUnit
import com.tftricks.app.domain.model.ComponentRequirement
import com.tftricks.app.domain.model.CompVariant
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.BoardCellData
import com.tftricks.app.ui.components.BoardGrid
import com.tftricks.app.ui.components.CommunityDragonDebugBanner
import com.tftricks.app.ui.components.ExpandableSection
import com.tftricks.app.ui.components.FavoriteButton
import com.tftricks.app.ui.components.GameIcon
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.PillChip
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.components.rememberCommunityDragonRepository
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
    val communityDragon = rememberCommunityDragonRepository()
    val championIcons by communityDragon.championIconUrls.collectAsStateWithLifecycle()
    val itemIcons by communityDragon.itemIconUrls.collectAsStateWithLifecycle()
    val traitIcons by communityDragon.traitIconUrls.collectAsStateWithLifecycle()
    val communityDragonStatus by communityDragon.status.collectAsStateWithLifecycle()
    val championMatchDebug by communityDragon.championMatchDebug.collectAsStateWithLifecycle()
    val loadedChampionIds by communityDragon.loadedChampionIds.collectAsStateWithLifecycle()
    var debugBannerDismissed by remember { mutableStateOf(false) }

    // Interstitial hook (no-op unless AdsConfig.INTERSTITIALS_ENABLED).
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        (context.applicationContext as TFTricksApplication).container.adsManager
            .onCompDetailOpened(context as? Activity)
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        val itemIconUrlsByName = remember(content.itemIdsByName, itemIcons) {
            content.itemIdsByName.mapNotNull { (name, id) -> itemIcons[id]?.let { name to it } }.toMap()
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (BuildConfig.DEBUG && !debugBannerDismissed) {
                        CommunityDragonDebugBanner(
                            status = communityDragonStatus,
                            onDismiss = { debugBannerDismissed = true }
                        )
                    }
                    CompHeader(comp, content.isFavorite, traitIcons, viewModel::toggleFavorite)
                    FinalBoardSection(
                        comp = comp,
                        championIcons = championIcons,
                        itemIconUrlsByName = itemIconUrlsByName,
                        championColor = championColor,
                        championChip = championChip
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailSections(
                        comp = comp,
                        championChip = championChip,
                        itemChip = itemChip,
                        requiredComponents = content.requiredComponents,
                        championMatchDebug = championMatchDebug,
                        loadedChampionIds = loadedChampionIds
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (BuildConfig.DEBUG && !debugBannerDismissed) {
                    CommunityDragonDebugBanner(
                        status = communityDragonStatus,
                        onDismiss = { debugBannerDismissed = true }
                    )
                }
                CompHeader(comp, content.isFavorite, traitIcons, viewModel::toggleFavorite)
                FinalBoardSection(
                    comp = comp,
                    championIcons = championIcons,
                    itemIconUrlsByName = itemIconUrlsByName,
                    championColor = championColor,
                    championChip = championChip
                )
                DetailSections(
                    comp = comp,
                    championChip = championChip,
                    itemChip = itemChip,
                    requiredComponents = content.requiredComponents,
                    championMatchDebug = championMatchDebug,
                    loadedChampionIds = loadedChampionIds
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompHeader(
    comp: TeamComp,
    isFavorite: Boolean,
    traitIcons: Map<String, String>,
    onToggleFavorite: () -> Unit
) {
    InfoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TierBadge(comp.tier)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comp.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    if (comp.traitsActive.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            comp.traitsActive.forEach { active ->
                                GameIcon(
                                    url = traitIcons[active.trait],
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "${comp.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }} • " +
                        "Patch ${comp.patchVersion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            FavoriteButton(isFavorite = isFavorite, onToggle = onToggleFavorite)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 10.dp)
        ) {
            comp.tags.forEach { PillChip(text = it, contentColor = BrandYellow) }
        }
    }
}

@Composable
private fun FinalBoardSection(
    comp: TeamComp,
    championIcons: Map<String, String>,
    itemIconUrlsByName: Map<String, String>,
    championColor: (String) -> Color,
    championChip: @Composable (String) -> Unit
) {
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
                        iconUrl = championIcons[unit.champion],
                        itemIconUrls = unit.items.mapNotNull { itemIconUrlsByName[it] },
                        starTarget = unit.starTarget
                    )
                }
            }
        )
        val flexUnits = comp.finalBoard.filter { it.position == null }
        if (flexUnits.isNotEmpty()) {
            SectionLabel(text = "Flexible position", modifier = Modifier.padding(top = 8.dp))
            UnitChipRow(flexUnits, championChip)
        }
    }
}

@Composable
private fun DetailSections(
    comp: TeamComp,
    championChip: @Composable (String) -> Unit,
    itemChip: @Composable (String) -> Unit,
    requiredComponents: List<ComponentRequirement>,
    championMatchDebug: Map<String, String>,
    loadedChampionIds: List<String>
) {
    if (comp.variants.isNotEmpty()) {
        ExpandableSection(title = "Variants") {
            comp.variants.forEachIndexed { index, variant ->
                VariantCard(variant, championChip)
                if (index != comp.variants.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    if (BuildConfig.DEBUG) {
        InfoCard {
            SectionLabel(text = "DEBUG: Champion match trace")
            Text(
                text = "This comp's ${comp.finalBoard.size} champions vs. the current matching logic:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
            comp.finalBoard.forEach { unit ->
                val matchedId = championMatchDebug[unit.champion]
                Text(
                    text = if (matchedId != null) {
                        "${unit.champion} → MATCHED: $matchedId"
                    } else {
                        "${unit.champion} → NO MATCH"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (matchedId != null) SuccessGreen else DangerRed,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            SectionLabel(
                text = "DEBUG: All ${loadedChampionIds.size} loaded CommunityDragon champion ids",
                modifier = Modifier.padding(top = 12.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(top = 6.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                loadedChampionIds.forEach { id ->
                    Text(
                        text = id,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (comp.levelingStages.isNotEmpty()) {
        ExpandableSection(title = "Leveling") {
            comp.levelingStages.forEachIndexed { index, stage ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandYellow
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    ExpandableSection(title = "Early Game") {
        if (comp.earlyGameNotes.isNotEmpty()) {
            comp.earlyGameNotes.forEach {
                BulletLine(text = it, bullet = "•", color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        SectionLabel(text = "Early game roster")
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
        LabeledParagraph("Leveling curve", comp.levelingGuide)
        LabeledParagraph("Economy", comp.economyGuide)
        LabeledParagraph("Roll timing", comp.rollTiming)
        LabeledParagraph("When to play", comp.whenToPlay)
    }

    if (comp.augmentGuide != null) {
        ExpandableSection(title = "Augments") {
            AugmentTierList("First pick", comp.augmentGuide.tier1)
            AugmentTierList("Second pick", comp.augmentGuide.tier2, topPadding = 10.dp)
            AugmentTierList("Third pick", comp.augmentGuide.tier3, topPadding = 10.dp)
        }
    }

    if (comp.carouselItemPriority.isNotEmpty()) {
        ExpandableSection(title = "Carousel Priority") {
            comp.carouselItemPriority.forEachIndexed { index, name ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandYellow
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    if (requiredComponents.isNotEmpty()) {
        ExpandableSection(title = "Component Items Required") {
            requiredComponents.forEach { req ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Text(
                        text = "×${req.count}",
                        style = MaterialTheme.typography.titleSmall,
                        color = BrandYellow
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = req.componentName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
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

    if (comp.tips.isNotEmpty()) {
        ExpandableSection(title = "Tips") {
            comp.tips.forEach {
                BulletLine(text = it, bullet = "•", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun AugmentTierList(label: String, augments: List<String>, topPadding: Dp = 0.dp) {
    if (augments.isEmpty()) return
    SectionLabel(text = label, modifier = Modifier.padding(top = topPadding))
    augments.forEach {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
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

/** An alternative setup of the parent comp — same core idea, different roster/itemization. */
@Composable
private fun VariantCard(
    variant: CompVariant,
    championChip: @Composable (String) -> Unit
) {
    InfoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TierBadge(variant.tier)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = variant.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
        }
        if (variant.carryChampions.isNotEmpty()) {
            SectionLabel(text = "Carries", modifier = Modifier.padding(top = 8.dp))
            ChipFlow(variant.carryChampions, championChip)
        }
        if (variant.tankChampions.isNotEmpty()) {
            SectionLabel(text = "Tanks", modifier = Modifier.padding(top = 8.dp))
            ChipFlow(variant.tankChampions, championChip)
        }
        if (variant.finalBoard.any { it.items.isNotEmpty() }) {
            SectionLabel(text = "Itemized units", modifier = Modifier.padding(top = 8.dp))
            variant.finalBoard.filter { it.items.isNotEmpty() }.forEach { unit ->
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
        if (variant.notes.isNotBlank()) {
            Text(
                text = variant.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (variant.statsNote.isNotBlank()) {
            Text(
                text = variant.statsNote,
                style = MaterialTheme.typography.labelSmall,
                color = BrandYellow,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
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
private fun BulletLine(text: String, bullet: String, color: Color) {
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
