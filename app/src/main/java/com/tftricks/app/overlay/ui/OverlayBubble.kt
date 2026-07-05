package com.tftricks.app.overlay.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.PureBlack

/**
 * The collapsed overlay: a small draggable yellow circle with the TFTricks spark.
 * Tap to expand; drag to move (the service moves the window and snaps to edges).
 */
@Composable
fun OverlayBubble(
    onTap: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .shadow(6.dp, CircleShape)
            .background(BrandYellow, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Black four-point spark, matching the brand mark.
        Canvas(modifier = Modifier.size(24.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val s = size.width / 2f
            val pinch = s * 0.24f
            val spark = Path().apply {
                moveTo(cx, cy - s)
                quadraticTo(cx + pinch, cy - pinch, cx + s, cy)
                quadraticTo(cx + pinch, cy + pinch, cx, cy + s)
                quadraticTo(cx - pinch, cy + pinch, cx - s, cy)
                quadraticTo(cx - pinch, cy - pinch, cx, cy - s)
                close()
            }
            drawPath(spark, PureBlack)
        }
    }
}
