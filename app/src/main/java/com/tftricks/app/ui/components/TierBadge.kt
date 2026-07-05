package com.tftricks.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tftricks.app.domain.model.Tier
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.TierA
import com.tftricks.app.ui.theme.TierB
import com.tftricks.app.ui.theme.TierC
import com.tftricks.app.ui.theme.TierS

/** Square badge showing a comp's tier (S/A/B/C) in its tier color. */
@Composable
fun TierBadge(tier: Tier, modifier: Modifier = Modifier) {
    val color = when (tier) {
        Tier.S -> TierS
        Tier.A -> TierA
        Tier.B -> TierB
        Tier.C -> TierC
    }
    Text(
        text = tier.name,
        color = PureBlack,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
        modifier = modifier
            .size(28.dp)
            .background(color, RoundedCornerShape(6.dp))
            .wrapContentSize(Alignment.Center)
    )
}
