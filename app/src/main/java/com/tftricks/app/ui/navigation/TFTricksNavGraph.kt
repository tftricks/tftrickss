package com.tftricks.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tftricks.app.ui.screens.augments.AugmentsScreen
import com.tftricks.app.ui.screens.builder.TeamBuilderScreen
import com.tftricks.app.ui.screens.champions.ChampionsScreen
import com.tftricks.app.ui.screens.home.HomeScreen
import com.tftricks.app.ui.screens.items.ItemsScreen
import com.tftricks.app.ui.screens.more.MoreScreen
import com.tftricks.app.ui.screens.patchnotes.PatchNotesScreen
import com.tftricks.app.ui.screens.saved.SavedCompsScreen
import com.tftricks.app.ui.screens.search.SearchScreen
import com.tftricks.app.ui.screens.settings.OverlaySettingsScreen
import com.tftricks.app.ui.screens.settings.SettingsScreen
import com.tftricks.app.ui.screens.comps.TeamCompsScreen
import com.tftricks.app.ui.screens.traits.TraitsScreen

@Composable
fun TFTricksNavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier
    ) {
        composable(Destination.Home.route) { HomeScreen(contentPadding) }
        composable(Destination.TeamComps.route) { TeamCompsScreen(contentPadding) }
        composable(Destination.Champions.route) { ChampionsScreen(contentPadding) }
        composable(Destination.TeamBuilder.route) { TeamBuilderScreen(contentPadding) }
        composable(Destination.More.route) {
            MoreScreen(
                contentPadding = contentPadding,
                onNavigate = { destination -> navController.navigate(destination.route) }
            )
        }
        composable(Destination.Traits.route) { TraitsScreen(contentPadding) }
        composable(Destination.Items.route) { ItemsScreen(contentPadding) }
        composable(Destination.Augments.route) { AugmentsScreen(contentPadding) }
        composable(Destination.SavedComps.route) { SavedCompsScreen(contentPadding) }
        composable(Destination.PatchNotes.route) { PatchNotesScreen(contentPadding) }
        composable(Destination.Search.route) { SearchScreen(contentPadding) }
        composable(Destination.Settings.route) { SettingsScreen(contentPadding) }
        composable(Destination.OverlaySettings.route) { OverlaySettingsScreen(contentPadding) }
    }
}
