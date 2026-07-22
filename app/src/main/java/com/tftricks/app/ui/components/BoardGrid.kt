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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.Dp
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
    val iconUrl: String? = null,
    /** Mini item icon URLs rendered under the hex (Lolchess-style), up to 3 shown. */
    val itemIconUrls: List<String> = emptyList(),
    /** Star badge shown on the hex when this unit is a 3★ target. */
    val starTarget: Int = 2
)

/**
 * The 4x7 TFT board, matching the in-game layout: row R1 (frontline, nearest the
 * opponent) renders at the top, R4 (backline) at the bottom, columns C1..C7 left to
 * right, with R2 and R4 offset half a cell to the right to suggest hexes. Position
 * indices follow [com.tftricks.app.domain.model.BoardUnit.position]'s
 * `(4-R)*7 + (C-1)` convention. Item mini-icons render under each occupied hex.
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
        val itemRowHeight = cellSize * 0.32f

        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            for (r in 1..4) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    if (r == 2 || r == 4) {
                        Spacer(modifier = Modifier.width(cellSize / 2))
                    }
                    for (c in 1..7) {
                        val position = (4 - r) * 7 + (c - 1)
                        val cell = cellFor(position)
                        val isSelected = position == selectedPosition
                        BoardHex(
                            cell = cell,
                            isSelected = isSelected,
                            cellSize = cellSize,
                            itemRowHeight = itemRowHeight,
                            onClick = onCellClick?.let { { it(position) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardHex(
    cell: BoardCellData?,
    isSelected: Boolean,
    cellSize: Dp,
    itemRowHeight: Dp,
    onClick: (() -> Unit)?
) {
    val shape = RoundedCornerShape(8.dp)
    val border = when {
        isSelected -> BorderStroke(2.dp, BrandYellow)
        cell != null -> BorderStroke(1.5.dp, cell.accentColor)
        else -> BorderStroke(1.dp, OutlineDark)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(cellSize)
                .background(SurfaceCard, shape)
                .border(border, shape)
                .let { if (onClick != null) it.clickable { onClick() } else it },
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
                if (cell.starTarget >= 3) {
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = (cellSize.value / 3.2f).sp),
                        color = BrandYellow,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                    )
                }
            }
        }
        if (cell != null && cell.itemIconUrls.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                cell.itemIconUrls.take(3).forEach { url ->
                    GameIcon(url = url, modifier = Modifier.size(itemRowHeight))
                }
            }
        } else {
            Spacer(modifier = Modifier.height(itemRowHeight).padding(top = 2.dp))
        }
    }
}
