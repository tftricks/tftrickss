package com.tftricks.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.tftricks.app.ads.AdsConfig

/**
 * Bottom banner slot for the main app screens (Home, Comps, Champions, Items).
 * Renders nothing until [AdsConfig.ADS_ENABLED] is flipped, then loads a real
 * AdMob banner with [AdsConfig.BANNER_UNIT_ID] — no dev-facing placeholder box
 * ships to real users.
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
    }
}
