package com.tftricks.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.SurfaceCard

/** What to render inside an occupied board cell. */
data class BoardCellData(
    val shortName: String,
    val accentColor: Color,
    /** CommunityDragon champion portrait URL; falls back to [shortName] text when null. */
    val iconUrl: String? = null
)

/**
 * The 4x7 TFT board (positions 0..27, row-major from the back row),
 * with odd rows offset half a cell to suggest hexes.
 * Read-only when [onCellClick] is null; interactive otherwise.
 */
@Composable
fun BoardGrid(
    cellFor: (Int) -> BoardCellData?,
    modifier: Modifier = Modifier,
    selectedPosition: Int? = null,
    onCellClick: ((Int) -> Unit)? = null
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gap = 3.dp
        // 7 cells + half-cell hex offset + 6 gaps must fit the available width.
        val cellSize = (maxWidth - gap * 6) / 7.5f

        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            for (row in 0 until 4) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    if (row % 2 == 1) {
                        Spacer(modifier = Modifier.width(cellSize / 2))
                    }
                    for (col in 0 until 7) {
                        val position = row * 7 + col
                        val cell = cellFor(position)
                        val isSelected = position == selectedPosition
                        val shape = RoundedCornerShape(8.dp)
                        val border = when {
                            isSelected -> BorderStroke(2.dp, BrandYellow)
                            cell != null -> BorderStroke(1.5.dp, cell.accentColor)
                            else -> BorderStroke(1.dp, OutlineDark)
                        }
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(SurfaceCard, shape)
                                .border(border, shape)
                                .let {
                                    if (onCellClick != null) {
                                        it.clickable { onCellClick(position) }
                                    } else it
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cell != null) {
                                if (cell.iconUrl != null) {
                                    AsyncImage(
                                        model = cell.iconUrl,
                                        contentDescription = cell.shortName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(shape)
                                    )
                                } else {
                                    Text(
                                        text = cell.shortName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = cell.accentColor,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(1.dp)
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
