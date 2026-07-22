package com.tftricks.app.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tftricks.app.domain.model.Augment
import com.tftricks.app.domain.model.Champion
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.OverlaySettings
import com.tftricks.app.domain.model.SavedTeam
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.Trait
import com.tftricks.app.overlay.OverlayPanelState
import com.tftricks.app.ui.components.GameIcon
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.screens.augments.color
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.SurfaceCard
import com.tftricks.app.ui.theme.SurfaceDark
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

/** Whatever's currently shown in the center column, regardless of which tab set it. */
private sealed interface OverlaySelection {
    data class CompSel(val comp: TeamComp) : OverlaySelection
    data class ItemSel(val item: Item) : OverlaySelection
    data class TraitSel(val trait: Trait) : OverlaySelection
    data class ChampionSel(val champion: Champion) : OverlaySelection
    data class AugmentSel(val augment: Augment) : OverlaySelection
    data class TeamSel(val team: SavedTeam) : OverlaySelection
}

/**
 * The expanded overlay: a full-screen, two-column Lolchess-style layout — a thin nav
 * rail on the left, the selected entry's detail in the center, and that category's
 * list on the right. Everything reads from [OverlayPanelState]'s pre-loaded flows, so
 * recompositions only touch small filtered lists.
 */
@Composable
fun OverlayPanel(
    panelState: OverlayPanelState,
    settings: OverlaySettings,
    onOpacityChange: (Float) -> Unit,
    onCollapse: () -> Unit
) {
    var tab by remember { mutableStateOf(OverlayTab.COMPS) }
    var query by remember { mutableStateOf("") }
    var selection by remember { mutableStateOf<OverlaySelection?>(null) }

    // Each tab owns its own list+detail pairing — start fresh when switching.
    LaunchedEffect(tab) {
        selection = null
        query = ""
    }

    val compact = settings.compactMode

    Box(modifier = Modifier.fillMaxSize().background(SurfaceDark)) {
        Row(modifier = Modifier.fillMaxSize()) {
            OverlayRail(
                selectedTab = tab,
                onSelectTab = { tab = it },
                opacity = settings.opacity,
                onOpacityChange = onOpacityChange
            )
            RailDivider()
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                OverlayCenterColumn(tab = tab, selection = selection, panelState = panelState, compact = compact)
            }
            RailDivider()
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .padding(10.dp)
            ) {
                OverlaySearchField(query = query, onQueryChange = { query = it }, compact = compact)
                Spacer(modifier = Modifier.height(6.dp))
                OverlayRightColumn(
                    tab = tab,
                    query = query,
                    panelState = panelState,
                    compact = compact,
                    selection = selection,
                    onSelect = { selection = it },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        }
        IconButton(
            onClick = onCollapse,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Collapse overlay",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun RailDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(OutlineDark)
    )
}

@Composable
private fun OverlayCenterColumn(
    tab: OverlayTab,
    selection: OverlaySelection?,
    panelState: OverlayPanelState,
    compact: Boolean
) {
    when (selection) {
        is OverlaySelection.CompSel -> OverlayCompDetail(
            comp = selection.comp,
            panelState = panelState,
            compact = compact,
            modifier = Modifier.fillMaxSize()
        )
        is OverlaySelection.ItemSel -> OverlayItemDetail(selection.item, compact)
        is OverlaySelection.TraitSel -> OverlayTraitDetail(selection.trait, panelState, compact)
        is OverlaySelection.ChampionSel -> OverlayChampionDetail(selection.champion, compact)
        is OverlaySelection.AugmentSel -> OverlayAugmentDetail(selection.augment, compact)
        is OverlaySelection.TeamSel -> OverlayTeamDetail(selection.team, panelState, compact)
        null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Select a ${tab.label.lowercase().removeSuffix("s")} from the list →",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun OverlaySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    compact: Boolean
) {
    val textStyle: TextStyle =
        (if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium)
            .copy(color = TextPrimary)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(BrandYellow),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(8.dp))
                    .border(1.dp, OutlineDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = if (compact) 6.dp else 9.dp)
            ) {
                if (query.isEmpty()) {
                    Text(text = "Filter…", style = textStyle.copy(color = TextSecondary))
                }
                innerTextField()
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverlayRightColumn(
    tab: OverlayTab,
    query: String,
    panelState: OverlayPanelState,
    compact: Boolean,
    selection: OverlaySelection?,
    onSelect: (OverlaySelection) -> Unit,
    modifier: Modifier = Modifier
) {
    val comps by panelState.comps.collectAsState()
    val champions by panelState.champions.collectAsState()
    val traits by panelState.traits.collectAsState()
    val items by panelState.items.collectAsState()
    val augments by panelState.augments.collectAsState()
    val favoriteCompIds by panelState.favoriteCompIds.collectAsState()
    val savedTeams by panelState.savedTeams.collectAsState()
    val championIcons by panelState.championIconUrls.collectAsState()
    val itemIcons by panelState.itemIconUrls.collectAsState()
    val traitIcons by panelState.traitIconUrls.collectAsState()

    val championsByName = remember(champions) { champions.associateBy { it.name } }
    val itemIconUrlByName = remember(items, itemIcons) {
        items.mapNotNull { i -> itemIcons[i.id]?.let { i.name to it } }.toMap()
    }

    val rowPadding = if (compact) 5.dp else 9.dp
    val titleStyle =
        if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall
    val bodyStyle =
        if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall

    fun String.matches() = query.isBlank() || contains(query, ignoreCase = true)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
    ) {
        when (tab) {
            OverlayTab.COMPS -> {
                items(comps.filter { it.name.matches() }, key = { it.id }) { comp ->
                    OverlayCompCard(
                        comp = comp,
                        isSelected = (selection as? OverlaySelection.CompSel)?.comp?.id == comp.id,
                        championIcons = championIcons,
                        itemIconUrlsByName = itemIconUrlByName,
                        traitIcons = traitIcons,
                        championsByName = championsByName,
                        onClick = { onSelect(OverlaySelection.CompSel(comp)) }
                    )
                }
            }

            OverlayTab.ITEMS -> {
                items(items.filter { it.name.matches() }, key = { it.id }) { item ->
                    OverlayRow(rowPadding, onClick = { onSelect(OverlaySelection.ItemSel(item)) }) {
                        GameIcon(url = itemIcons[item.id], modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(item.name, style = titleStyle, color = TextPrimary)
                            Text(
                                text = if (item.components.isEmpty()) "Component"
                                else item.components.joinToString(" + "),
                                style = bodyStyle,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            OverlayTab.TRAITS -> {
                items(traits.filter { it.name.matches() }, key = { it.id }) { trait ->
                    OverlayRow(rowPadding, onClick = { onSelect(OverlaySelection.TraitSel(trait)) }) {
                        GameIcon(url = traitIcons[trait.name], modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${trait.name}  (${trait.breakpoints.joinToString("/") { it.count.toString() }})",
                                style = titleStyle,
                                color = BrandYellow
                            )
                            Text(
                                text = trait.champions.joinToString(" • "),
                                style = bodyStyle,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            OverlayTab.CHAMPIONS -> {
                items(champions.filter { it.name.matches() }, key = { it.id }) { champion ->
                    OverlayRow(rowPadding, onClick = { onSelect(OverlaySelection.ChampionSel(champion)) }) {
                        GameIcon(
                            url = championIcons[champion.name],
                            borderColor = costColor(champion.cost),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(champion.name, style = titleStyle, color = TextPrimary)
                            Text(
                                text = champion.traits.joinToString(" • "),
                                style = bodyStyle,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            OverlayTab.AUGMENTS -> {
                items(augments.filter { it.name.matches() }, key = { it.id }) { augment ->
                    OverlayRow(rowPadding, onClick = { onSelect(OverlaySelection.AugmentSel(augment)) }) {
                        Column {
                            Text(augment.name, style = titleStyle, color = augment.tier.color())
                            Text(
                                text = "Priority " + "★".repeat(augment.priorityRating) +
                                    "☆".repeat(5 - augment.priorityRating),
                                style = bodyStyle,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            OverlayTab.FAVORITES -> {
                val favoriteComps = comps.filter { it.id in favoriteCompIds && it.name.matches() }
                val teams = savedTeams.filter { it.name.matches() }
                if (favoriteComps.isEmpty() && teams.isEmpty()) {
                    item(key = "fav_empty") {
                        Text(
                            text = "No favorites yet. Heart comps in the app or save teams in the Team Builder.",
                            style = bodyStyle,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
                items(favoriteComps, key = { "fav_${it.id}" }) { comp ->
                    OverlayCompCard(
                        comp = comp,
                        isSelected = (selection as? OverlaySelection.CompSel)?.comp?.id == comp.id,
                        championIcons = championIcons,
                        itemIconUrlsByName = itemIconUrlByName,
                        traitIcons = traitIcons,
                        championsByName = championsByName,
                        onClick = { onSelect(OverlaySelection.CompSel(comp)) }
                    )
                }
                items(teams, key = { "team_${it.id}" }) { team ->
                    OverlayRow(rowPadding, onClick = { onSelect(OverlaySelection.TeamSel(team)) }) {
                        Column {
                            Text(team.name, style = titleStyle, color = BrandYellow)
                            Text(
                                text = "${team.units.size} units",
                                style = bodyStyle,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayRow(
    padding: Dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceCard, shape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 10.dp, vertical = padding)
    ) {
        content()
    }
}

// ---- Simple center-column detail views for the non-comp tabs ---------------------

@Composable
private fun OverlayItemDetail(item: Item, compact: Boolean) {
    val titleStyle = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val bodyStyle = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(item.name, style = titleStyle, color = TextPrimary)
        SectionLabel(text = "Recipe", modifier = Modifier.padding(top = 8.dp))
        Text(
            text = if (item.components.isEmpty()) "Base component" else item.components.joinToString(" + "),
            style = bodyStyle,
            color = TextSecondary
        )
        SectionLabel(text = "Effect", modifier = Modifier.padding(top = 8.dp))
        Text(item.effect, style = bodyStyle, color = TextSecondary)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverlayTraitDetail(trait: Trait, panelState: OverlayPanelState, compact: Boolean) {
    val championIcons by panelState.championIconUrls.collectAsState()
    val champions by panelState.champions.collectAsState()
    val costByName = remember(champions) { champions.associate { it.name to it.cost } }
    val titleStyle = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val bodyStyle = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(trait.name, style = titleStyle, color = BrandYellow)
        Text(trait.description, style = bodyStyle, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
        SectionLabel(text = "Breakpoints", modifier = Modifier.padding(top = 8.dp))
        trait.breakpoints.forEach { bp ->
            Text("${bp.count}: ${bp.effect}", style = bodyStyle, color = TextSecondary)
        }
        SectionLabel(text = "Champions", modifier = Modifier.padding(top = 8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            trait.champions.forEach { name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GameIcon(
                        url = championIcons[name],
                        borderColor = costColor(costByName[name] ?: 1),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(name, style = bodyStyle, color = costColor(costByName[name] ?: 1))
                }
            }
        }
    }
}

@Composable
private fun OverlayChampionDetail(champion: Champion, compact: Boolean) {
    val titleStyle = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val bodyStyle = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("${champion.cost}g  ${champion.name}", style = titleStyle, color = costColor(champion.cost))
        Text(champion.traits.joinToString(" • "), style = bodyStyle, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
        SectionLabel(text = "Ability", modifier = Modifier.padding(top = 8.dp))
        Text(champion.ability.name, style = titleStyle, color = TextPrimary)
        Text(champion.ability.description, style = bodyStyle, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun OverlayAugmentDetail(augment: Augment, compact: Boolean) {
    val titleStyle = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val bodyStyle = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(augment.name, style = titleStyle, color = augment.tier.color())
        Text(
            text = "Priority " + "★".repeat(augment.priorityRating) + "☆".repeat(5 - augment.priorityRating),
            style = bodyStyle,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
        SectionLabel(text = "Effect", modifier = Modifier.padding(top = 8.dp))
        Text(augment.effect, style = bodyStyle, color = TextSecondary)
        if (augment.notes.isNotBlank()) {
            SectionLabel(text = "Notes", modifier = Modifier.padding(top = 8.dp))
            Text(augment.notes, style = bodyStyle, color = TextSecondary)
        }
    }
}

@Composable
private fun OverlayTeamDetail(team: SavedTeam, panelState: OverlayPanelState, compact: Boolean) {
    val champions by panelState.champions.collectAsState()
    val championIcons by panelState.championIconUrls.collectAsState()
    val championsById = remember(champions) { champions.associateBy { it.id } }
    val titleStyle = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val bodyStyle = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodyMedium
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(team.name, style = titleStyle, color = BrandYellow)
        SectionLabel(text = "Roster", modifier = Modifier.padding(top = 8.dp))
        team.units.sortedBy { it.position }.forEach { unit ->
            val champion = championsById[unit.championId]
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                GameIcon(
                    url = champion?.name?.let { championIcons[it] },
                    borderColor = costColor(champion?.cost ?: 1),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = champion?.name ?: unit.championId,
                    style = bodyStyle,
                    color = costColor(champion?.cost ?: 1)
                )
            }
        }
    }
}
