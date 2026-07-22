package com.tftricks.app.overlay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/** Categories reachable from the overlay's left rail. */
enum class OverlayTab(val label: String, val icon: ImageVector) {
    COMPS("Comps", Icons.AutoMirrored.Filled.List),
    SCOUT("Scout", Icons.Filled.Search)
}
