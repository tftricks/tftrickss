package com.tftricks.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.tftricks.app.BuildConfig

/**
 * Every navigable screen in the app.
 *
 * @param title full name shown in the top app bar.
 * @param label short name shown under bottom-bar icons.
 */
enum class Destination(
    val route: String,
    val title: String,
    val label: String,
    val icon: ImageVector
) {
    Home("home", "TFTricks", "Home", Icons.Filled.Home),
    TeamComps("team_comps", "Team Comps", "Comps", Icons.AutoMirrored.Filled.List),
    Champions("champions", "Champions", "Champs", Icons.Filled.Person),
    TeamBuilder("team_builder", "Team Builder", "Builder", Icons.Filled.Edit),
    More("more", "More", "More", Icons.Filled.Menu),

    Traits("traits", "Traits", "Traits", Icons.Filled.Star),
    Items("items", "Items", "Items", Icons.Filled.Build),
    Augments("augments", "Augments", "Augments", Icons.Filled.AddCircle),
    SavedComps("saved_comps", "Saved Comps", "Saved", Icons.Filled.Favorite),
    PatchNotes("patch_notes", "Patch Notes", "Patches", Icons.Filled.Info),
    Search("search", "Search", "Search", Icons.Filled.Search),
    Settings("settings", "Settings", "Settings", Icons.Filled.Settings),
    OverlaySettings("overlay_settings", "Overlay Settings", "Overlay", Icons.Filled.Notifications),
    CrashLog("crash_log", "Crash Log", "Crash Log", Icons.Filled.Warning);

    companion object {
        /** Top-level tabs shown in the bottom navigation bar. */
        val bottomBarDestinations = listOf(Home, TeamComps, Champions, TeamBuilder, More)

        /** Secondary screens reachable from the More hub. Crash Log only shows in debug builds. */
        val moreDestinations = listOfNotNull(
            Traits, Items, Augments, SavedComps, PatchNotes, Search, Settings, OverlaySettings,
            CrashLog.takeIf { BuildConfig.DEBUG }
        )

        fun fromRoute(route: String?): Destination? = entries.find { it.route == route }
    }
}
