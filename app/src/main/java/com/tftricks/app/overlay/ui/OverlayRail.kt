package com.tftricks.app.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.SurfaceDark
import com.tftricks.app.ui.theme.TextSecondary

/** Categories reachable from the overlay's left rail. */
enum class OverlayTab(val label: String, val icon: ImageVector) {
    COMPS("Comps", Icons.AutoMirrored.Filled.List),
    ITEMS("Items", Icons.Filled.Build),
    TRAITS("Traits", Icons.Filled.Star),
    CHAMPIONS("Champs", Icons.Filled.Person),
    AUGMENTS("Augs", Icons.Filled.AddCircle),
    FAVORITES("Favs", Icons.Filled.Favorite)
}

/**
 * Thin left navigation rail: category icons on top, an opacity slider pinned to the
 * bottom. The slider tracks a local value while dragging (for a lag-free thumb) and
 * only commits to [onOpacityChange] — which persists to DataStore and re-applies the
 * window's alpha — once the drag ends.
 */
@Composable
fun OverlayRail(
    selectedTab: OverlayTab,
    onSelectTab: (OverlayTab) -> Unit,
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(64.dp)
            .background(SurfaceDark),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        OverlayTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            IconButton(
                onClick = { onSelectTab(tab) },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (selected) BrandYellow else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        var sliderValue by remember(opacity) { mutableFloatStateOf(opacity) }
        Text(
            text = "${(sliderValue * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        VerticalSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onOpacityChange(sliderValue) },
            valueRange = 0.15f..1f,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(140.dp)
                .width(32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** A [Slider] rotated to run bottom-to-top, sized to fit a narrow rail. */
@Composable
private fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        colors = SliderDefaults.colors(
            thumbColor = BrandYellow,
            activeTrackColor = BrandYellow,
            inactiveTrackColor = OutlineDark
        ),
        modifier = modifier
            .graphicsLayer {
                rotationZ = -90f
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth
                    )
                )
                layout(placeable.height, placeable.width) {
                    placeable.place(-placeable.width, 0)
                }
            }
    )
}
