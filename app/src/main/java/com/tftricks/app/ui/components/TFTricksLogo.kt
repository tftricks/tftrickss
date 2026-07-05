package com.tftricks.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tftricks.app.ui.theme.BrandYellow

/**
 * PLACEHOLDER logo: a yellow hex shield with a spark, plus the wordmark.
 * Will be swapped for the real TFTricks logo asset later — keep the API
 * (size + showWordmark) stable so call sites don't change.
 */
@Composable
fun TFTricksLogo(
    modifier: Modifier = Modifier,
    markSize: Dp = 96.dp,
    showWordmark: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(markSize)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Hex shield outline
            val rx = w * 0.34f
            val ry = h * 0.38f
            val hex = Path().apply {
                moveTo(cx, cy - ry)
                lineTo(cx + rx, cy - ry * 0.5f)
                lineTo(cx + rx, cy + ry * 0.5f)
                lineTo(cx, cy + ry)
                lineTo(cx - rx, cy + ry * 0.5f)
                lineTo(cx - rx, cy - ry * 0.5f)
                close()
            }
            drawPath(
                path = hex,
                color = BrandYellow,
                style = Stroke(width = w * 0.055f, join = StrokeJoin.Miter)
            )

            // Four-point spark in the center
            val s = w * 0.17f
            val pinch = s * 0.22f
            val spark = Path().apply {
                moveTo(cx, cy - s)
                quadraticTo(cx + pinch, cy - pinch, cx + s, cy)
                quadraticTo(cx + pinch, cy + pinch, cx, cy + s)
                quadraticTo(cx - pinch, cy + pinch, cx - s, cy)
                quadraticTo(cx - pinch, cy - pinch, cx, cy - s)
                close()
            }
            drawPath(path = spark, color = BrandYellow)

            // Crown peaks above the shield
            val crownBase = cy - ry - h * 0.02f
            val peak = h * 0.14f
            val crown = Path().apply {
                moveTo(cx - rx * 0.7f, crownBase)
                lineTo(cx - rx * 0.45f, crownBase - peak)
                lineTo(cx - rx * 0.15f, crownBase - peak * 0.35f)
                lineTo(cx, crownBase - peak * 1.2f)
                lineTo(cx + rx * 0.15f, crownBase - peak * 0.35f)
                lineTo(cx + rx * 0.45f, crownBase - peak)
                lineTo(cx + rx * 0.7f, crownBase)
                close()
            }
            drawPath(path = crown, color = BrandYellow)
        }

        if (showWordmark) {
            Text(
                text = "TFTricks",
                color = BrandYellow,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            )
        }
    }
}
