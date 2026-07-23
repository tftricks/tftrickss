package com.tftricks.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.SurfaceElevated

/**
 * A champion/item portrait loaded from CommunityDragon. Renders an empty tinted tile
 * (no crash, no broken-image icon) when [url] is null — e.g. before the first
 * successful CommunityDragon fetch, or when a local entry has no name match.
 */
@Composable
fun GameIcon(
    url: String?,
    modifier: Modifier = Modifier,
    borderColor: Color = OutlineDark,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(SurfaceElevated, shape)
            .border(1.dp, borderColor, shape)
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
