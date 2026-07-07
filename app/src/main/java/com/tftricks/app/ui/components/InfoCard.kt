package com.tftricks.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.SurfaceCard

/**
 * Standard rounded dark card used by every list screen.
 * Pass [onClick] for tappable cards — it uses the clickable Card overload,
 * which gives a proper bounded ripple/press state.
 */
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    val colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    val border = BorderStroke(1.dp, OutlineDark)
    val innerPadding = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            border = border
        ) {
            Column(modifier = innerPadding, content = content)
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            border = border
        ) {
            Column(modifier = innerPadding, content = content)
        }
    }
}
