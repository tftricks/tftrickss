package com.tftricks.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Thin abstraction over the AdMob SDK so screens never touch it directly.
 * Everything is a no-op while the [AdsConfig] flags are off, which keeps the
 * whole ads surface behind a single switch.
 */
class AdsManager(private val context: Context) {

    private var initialized = false
    private var interstitial: InterstitialAd? = null
    private var compDetailOpens = 0

    /** Called once from the Application class. No-op while ads are disabled. */
    fun initialize() {
        if (!AdsConfig.ADS_ENABLED || initialized) return
        initialized = true
        MobileAds.initialize(context)
        if (AdsConfig.INTERSTITIALS_ENABLED) preloadInterstitial()
    }

    /**
     * Interstitial hook: call when a comp detail screen opens. Shows a preloaded
     * interstitial every [AdsConfig.INTERSTITIAL_EVERY_N_COMP_DETAILS] opens.
     * Disabled by default via [AdsConfig.INTERSTITIALS_ENABLED].
     */
    fun onCompDetailOpened(activity: Activity?) {
        if (!AdsConfig.ADS_ENABLED || !AdsConfig.INTERSTITIALS_ENABLED) return
        compDetailOpens++
        if (compDetailOpens % AdsConfig.INTERSTITIAL_EVERY_N_COMP_DETAILS != 0) return
        val ad = interstitial ?: return
        interstitial = null
        if (activity != null) ad.show(activity)
        preloadInterstitial()
    }

    private fun preloadInterstitial() {
        InterstitialAd.load(
            context,
            AdsConfig.INTERSTITIAL_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            }
        )
    }
}
