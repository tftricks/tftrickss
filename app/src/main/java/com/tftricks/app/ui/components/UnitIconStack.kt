package com.tftricks.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tftricks.app.ui.theme.BrandYellow

/**
 * A champion icon with a 3★-target star badge and a strip of mini item icons
 * underneath, Lolchess-style. Used for both the comps list roster row and the
 * Final Board hexes.
 */
@Composable
fun UnitIconStack(
    championIconUrl: String?,
    accentColor: Color,
    starTarget: Int,
    itemIconUrls: List<String>,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box {
            GameIcon(url = championIconUrl, borderColor = accentColor, modifier = Modifier.size(iconSize))
            if (starTarget >= 3) {
                Text(
                    text = "★",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = (iconSize.value / 3.2f).sp),
                    color = BrandYellow,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = (-4).dp, end = (-4).dp)
                )
            }
        }
        if (itemIconUrls.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.padding(top = 2.dp)) {
                itemIconUrls.take(3).forEach { url ->
                    GameIcon(url = url, modifier = Modifier.size(iconSize / 3f))
                }
            }
        }
    }
}
