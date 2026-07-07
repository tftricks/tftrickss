package com.tftricks.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.tftricks.app.ads.AdsConfig
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.TextSecondary

/**
 * Bottom banner slot for the main app screens (Home, Comps, Champions, Items).
 * Renders a neutral placeholder box until [AdsConfig.ADS_ENABLED] is flipped,
 * then loads a real AdMob banner with [AdsConfig.BANNER_UNIT_ID].
 *
 * Never place this inside the in-game overlay.
 */
@Composable
fun BannerAdSlot(modifier: Modifier = Modifier) {
    if (AdsConfig.ADS_ENABLED) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdsConfig.BANNER_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(BorderStroke(1.dp, OutlineDark), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ad placeholder",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
