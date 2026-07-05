package com.tftricks.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tftricks.app.ui.theme.SurfaceElevated
import com.tftricks.app.ui.theme.TextPrimary

/** Small rounded label for tags, trait names, champions, items… Tappable when [onClick] is set. */
@Composable
fun PillChip(
    text: String,
    modifier: Modifier = Modifier,
    contentColor: Color = TextPrimary,
    containerColor: Color = SurfaceElevated,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(50)
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        modifier = modifier
            .clip(shape)
            .background(containerColor, shape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}
