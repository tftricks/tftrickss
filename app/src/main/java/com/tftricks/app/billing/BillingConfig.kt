package com.tftricks.app.billing

/**
 * Central IAP configuration. TFTricks ships fully free at launch — every comp and
 * feature is open, no paywall. This flag exists so a future "remove ads" purchase
 * (see README's roadmap) can be wired in later without re-deriving where it plugs in.
 *
 * HOW TO GO LIVE WITH REAL BILLING:
 * 1. Add the Google Play Billing Library dependency and implement the purchase flow
 *    for a single non-consumable "remove ads" product.
 * 2. Have a successful purchase set a `DataStore` flag that [com.tftricks.app.ads.AdsManager]
 *    and `BannerAdSlot` check before showing any ad.
 * 3. Flip [IAP_ENABLED] to true — this un-hides the "Remove ads" row in Settings.
 */
object BillingConfig {
    /** Master switch. While false, all IAP-related UI (the "Remove ads" row) stays hidden. */
    const val IAP_ENABLED = false
}
