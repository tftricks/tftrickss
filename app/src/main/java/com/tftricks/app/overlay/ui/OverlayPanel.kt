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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.tftricks.app.domain.model.OverlaySettings
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.overlay.OverlayPanelState
import com.tftricks.app.ui.components.GameIcon
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.screens.augments.color
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.SurfaceCard
import com.tftricks.app.ui.theme.SurfaceDark
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

private enum class OverlayTab(val label: String) {
    COMPS("Comps"),
    ITEMS("Items"),
    TRAITS("Traits"),
    CHAMPIONS("Champs"),
    AUGMENTS("Augs"),
    FAVORITES("Favs")
}

/**
 * The expanded overlay: search + tabs over the offline database, with a compact
 * comp detail view. Everything reads from [OverlayPanelState]'s pre-loaded flows,
 * so recompositions only touch small filtered lists.
 */
@Composable
fun OverlayPanel(
    panelState: OverlayPanelState,
    settings: OverlaySettings,
    onCollapse: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(OverlayTab.COMPS) }
    var selectedComp by remember { mutableStateOf<TeamComp?>(null) }

    val compact = settings.compactMode
    val background =
        if (settings.transparentBackground) PureBlack.copy(alpha = 0.62f) else SurfaceDark
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(background, shape)
            .border(1.dp, OutlineDark, shape)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TFTricks",
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandYellow,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Collapse overlay",
                        tint = TextSecondary
                    )
                }
            }

            val comp = selectedComp
            if (comp != null) {
                OverlayCompDetail(
                    comp = comp,
                    panelState = panelState,
                    compact = compact,
                    onBack = { selectedComp = null },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            } else {
                OverlaySearchField(
                    query = query,
                    onQueryChange = { query = it },
                    compact = compact
                )

                ScrollableTabRow(
                    selectedTabIndex = tab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = BrandYellow,
                    edgePadding = 0.dp,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    OverlayTab.entries.forEach { t ->
                        Tab(
                            selected = tab == t,
                            onClick = { tab = t },
                            text = { Text(t.label, style = MaterialTheme.typography.labelMedium) },
                            selectedContentColor = BrandYellow,
                            unselectedContentColor = TextSecondary
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    OverlayTabContent(
                        tab = tab,
                        query = query,
                        panelState = panelState,
                        compact = compact,
                        onOpenComp = { selectedComp = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
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
                    Text(
                        text = "Filter this tab…",
                        style = textStyle.copy(color = TextSecondary)
                    )
                }
                innerTextField()
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverlayTabContent(
    tab: OverlayTab,
    query: String,
    panelState: OverlayPanelState,
    compact: Boolean,
    onOpenComp: (TeamComp) -> Unit,
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
                    OverlayRow(rowPadding, onClick = { onOpenComp(comp) }) {
                        TierBadge(comp.tier)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(comp.name, style = titleStyle, color = TextPrimary)
                            if (!compact) {
                                Text(
                                    text = comp.tags.joinToString(" • "),
                                    style = bodyStyle,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            OverlayTab.ITEMS -> {
                items(items.filter { it.name.matches() }, key = { it.id }) { item ->
                    OverlayRow(rowPadding) {
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
                    OverlayRow(rowPadding) {
                        Column {
                            Text(
                                text = "${trait.name}  (${trait.breakpoints.joinToString("/") { it.count.toString() }})",
                                style = titleStyle,
                                color = BrandYellow
                            )
                            Text(
                                text = trait.champions.joinToString(" • "),
                                style = bodyStyle,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            OverlayTab.CHAMPIONS -> {
                items(champions.filter { it.name.matches() }, key = { it.id }) { champion ->
                    OverlayRow(rowPadding) {
                        GameIcon(
                            url = championIcons[champion.name],
                            borderColor = costColor(champion.cost),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${champion.cost}g",
                            style = titleStyle,
                            color = costColor(champion.cost)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
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
                    OverlayRow(rowPadding) {
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
                    OverlayRow(rowPadding, onClick = { onOpenComp(comp) }) {
                        TierBadge(comp.tier)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(comp.name, style = titleStyle, color = TextPrimary)
                    }
                }
                items(teams, key = { "team_${it.id}" }) { team ->
                    val championsById = remember(champions) { champions.associateBy { it.id } }
                    OverlayRow(rowPadding) {
                        Column {
                            Text(team.name, style = titleStyle, color = BrandYellow)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                team.units.sortedBy { it.position }.forEach { unit ->
                                    val champion = championsById[unit.championId]
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        GameIcon(
                                            url = champion?.name?.let { championIcons[it] },
                                            borderColor = costColor(champion?.cost ?: 1),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = champion?.name ?: unit.championId,
                                            style = bodyStyle,
                                            color = costColor(champion?.cost ?: 1)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayRow(
    padding: androidx.compose.ui.unit.Dp,
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
