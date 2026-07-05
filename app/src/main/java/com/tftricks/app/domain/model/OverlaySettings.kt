package com.tftricks.app.domain.model

/**
 * User customization of the in-game overlay, persisted locally and applied live.
 */
data class OverlaySettings(
    /** Window alpha, 0.4..1.0. */
    val opacity: Float = 1.0f,
    val panelSize: PanelSize = PanelSize.MEDIUM,
    /** Semi-transparent panel background so the game stays visible behind it. */
    val transparentBackground: Boolean = false,
    /** Denser lists and smaller text inside the overlay panel. */
    val compactMode: Boolean = false,
    /** Last position of the collapsed floating button (window coordinates). */
    val buttonX: Int = 0,
    val buttonY: Int = 320
)

enum class PanelSize(val heightFraction: Float) {
    SMALL(0.42f),
    MEDIUM(0.58f),
    LARGE(0.74f)
}
