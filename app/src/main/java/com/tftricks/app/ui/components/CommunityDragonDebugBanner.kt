package com.tftricks.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tftricks.app.data.remote.CommunityDragonStatus
import com.tftricks.app.data.remote.debugLabel
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.DangerRed
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.SuccessGreen

/**
 * Debug-build-only banner showing the live [CommunityDragonStatus]. Meant to make
 * CommunityDragon fetch/parse failures visible on-device without a computer or Logcat.
 */
@Composable
fun CommunityDragonDebugBanner(
    status: CommunityDragonStatus,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when (status) {
        is CommunityDragonStatus.Loading -> BrandYellow
        is CommunityDragonStatus.Success -> SuccessGreen
        is CommunityDragonStatus.Error -> DangerRed
    }
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DEBUG · CommunityDragon: ${status.debugLabel()}",
            style = MaterialTheme.typography.labelSmall,
            color = PureBlack,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss, modifier = Modifier.padding(start = 4.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Dismiss debug banner",
                tint = PureBlack
            )
        }
    }
}
