package com.tftricks.app.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.tftricks.app.domain.model.OverlaySettings
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.overlay.OverlayPanelState
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.SurfaceCard
import com.tftricks.app.ui.theme.SurfaceDark
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary

/**
 * The expanded overlay: a full-screen, two-column Lolchess-style layout — a thin rail
 * on the left (collapse + opacity only; the overlay browses comps exclusively, so
 * there's no category navigation), the selected comp's detail in the center, and the
 * comp list on the right. Everything reads from [OverlayPanelState]'s pre-loaded
 * flows, so recompositions only touch small filtered lists.
 */
@Composable
fun OverlayPanel(
    panelState: OverlayPanelState,
    settings: OverlaySettings,
    onOpacityChange: (Float) -> Unit,
    onCollapse: () -> Unit
) {
    val compact = settings.compactMode
    val comps by panelState.comps.collectAsState()
    val selectedComp = comps.find { it.id == panelState.selectedCompId }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceDark)) {
        Row(modifier = Modifier.fillMaxSize()) {
            OverlayRail(
                opacity = settings.opacity,
                onOpacityChange = onOpacityChange,
                onCollapse = onCollapse
            )
            RailDivider()
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                OverlayCenterColumn(selection = selectedComp, panelState = panelState, compact = compact)
            }
            RailDivider()
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .padding(10.dp)
            ) {
                OverlaySearchField(
                    query = panelState.searchQuery,
                    onQueryChange = { panelState.searchQuery = it },
                    compact = compact
                )
                Spacer(modifier = Modifier.height(6.dp))
                OverlayRightColumn(
                    query = panelState.searchQuery,
                    panelState = panelState,
                    compact = compact,
                    selectedCompId = panelState.selectedCompId,
                    onSelect = { panelState.selectComp(it.id) },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
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
    selection: TeamComp?,
    panelState: OverlayPanelState,
    compact: Boolean
) {
    if (selection != null) {
        OverlayCompDetail(
            comp = selection,
            panelState = panelState,
            compact = compact,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Select a comp from the list →",
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
                    Text(text = "Filter comps…", style = textStyle.copy(color = TextSecondary))
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun OverlayRightColumn(
    query: String,
    panelState: OverlayPanelState,
    compact: Boolean,
    selectedCompId: String?,
    onSelect: (TeamComp) -> Unit,
    modifier: Modifier = Modifier
) {
    val comps by panelState.comps.collectAsState()
    val champions by panelState.champions.collectAsState()
    val items by panelState.items.collectAsState()
    val championIcons by panelState.championIconUrls.collectAsState()
    val itemIcons by panelState.itemIconUrls.collectAsState()
    val traitIcons by panelState.traitIconUrls.collectAsState()

    val championsByName = remember(champions) { champions.associateBy { it.name } }
    val itemIconUrlByName = remember(items, itemIcons) {
        items.mapNotNull { i -> itemIcons[i.id]?.let { i.name to it } }.toMap()
    }

    val filteredComps = remember(comps, query) {
        comps.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
    }

    LazyColumn(
        state = panelState.rightListState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
    ) {
        items(filteredComps, key = { it.id }) { comp ->
            OverlayCompCard(
                comp = comp,
                isSelected = selectedCompId == comp.id,
                championIcons = championIcons,
                itemIconUrlsByName = itemIconUrlByName,
                traitIcons = traitIcons,
                championsByName = championsByName,
                onClick = { onSelect(comp) }
            )
        }
    }
}
