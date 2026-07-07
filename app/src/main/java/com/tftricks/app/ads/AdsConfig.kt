package com.tftricks.app.ads

/**
 * Central ads configuration.
 *
 * HOW TO GO LIVE WITH REAL ADS:
 * 1. Replace [ADMOB_APP_ID] below AND the matching
 *    `com.google.android.gms.ads.APPLICATION_ID` meta-data in
 *    app/src/main/AndroidManifest.xml with your real AdMob app id.
 * 2. Replace [BANNER_UNIT_ID] / [INTERSTITIAL_UNIT_ID] with your real unit ids.
 * 3. Flip [ADS_ENABLED] (and optionally [INTERSTITIALS_ENABLED]) to true.
 *
 * The values below are Google's official public TEST ids — safe to ship to
 * testers, never earns revenue.
 *
 * Ads are NEVER shown inside the in-game overlay ([com.tftricks.app.overlay]) —
 * do not add ad slots there.
 */
object AdsConfig {
    /** Master switch. While false, banner slots render a neutral placeholder box. */
    const val ADS_ENABLED = false

    /** Interstitial after every [INTERSTITIAL_EVERY_N_COMP_DETAILS] comp detail opens. */
    const val INTERSTITIALS_ENABLED = false
    const val INTERSTITIAL_EVERY_N_COMP_DETAILS = 5

    // Google test ids — replace with your own before release.
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
}
