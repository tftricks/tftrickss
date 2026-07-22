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
