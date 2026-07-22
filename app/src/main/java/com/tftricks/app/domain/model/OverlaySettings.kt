package com.tftricks.app.domain.model

/**
 * User customization of the in-game overlay, persisted locally and applied live.
 */
data class OverlaySettings(
    /** Window alpha for the whole expanded panel (background + content together),
     *  low enough to let the game show through while still interacting with the overlay. */
    val opacity: Float = 0.9f,
    /** Denser lists and smaller text inside the overlay panel. */
    val compactMode: Boolean = false,
    /** Last position of the collapsed floating button (window coordinates). */
    val buttonX: Int = 0,
    val buttonY: Int = 320
)

/**
 * The overlay panel's last-seen screen: which comp was open and where the user had
 * scrolled to. Persisted so collapsing to the bubble (or a service restart) restores
 * exactly where the user left off, instead of resetting to the comp list. Cleared
 * when the overlay service is fully stopped rather than just collapsed.
 */
data class OverlaySession(
    val selectedCompId: String? = null,
    val centerScrollOffset: Int = 0,
    val listIndex: Int = 0,
    val listOffset: Int = 0
)
