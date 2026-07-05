package com.tftricks.app.overlay.ui

import androidx.compose.runtime.Composable
import com.tftricks.app.domain.model.OverlaySettings
import com.tftricks.app.overlay.OverlayPanelState
import com.tftricks.app.ui.theme.TFTricksTheme

/** Root of the overlay window: floating bubble when collapsed, panel when expanded. */
@Composable
fun OverlayContent(
    expanded: Boolean,
    settings: OverlaySettings,
    panelState: OverlayPanelState,
    onBubbleTap: () -> Unit,
    onBubbleDrag: (dx: Float, dy: Float) -> Unit,
    onBubbleDragEnd: () -> Unit,
    onCollapse: () -> Unit
) {
    TFTricksTheme {
        if (expanded) {
            OverlayPanel(
                panelState = panelState,
                settings = settings,
                onCollapse = onCollapse
            )
        } else {
            OverlayBubble(
                onTap = onBubbleTap,
                onDrag = onBubbleDrag,
                onDragEnd = onBubbleDragEnd
            )
        }
    }
}
