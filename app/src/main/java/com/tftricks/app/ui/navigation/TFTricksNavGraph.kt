package com.tftricks.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tftricks.app.ui.screens.augments.AugmentsScreen
import com.tftricks.app.ui.screens.builder.TeamBuilderScreen
import com.tftricks.app.ui.screens.champions.ChampionDetailScreen
import com.tftricks.app.ui.screens.champions.ChampionsScreen
import com.tftricks.app.ui.screens.comps.CompDetailScreen
import com.tftricks.app.ui.screens.comps.TeamCompsScreen
import com.tftricks.app.ui.screens.crashlog.CrashLogScreen
import com.tftricks.app.ui.screens.home.HomeScreen
import com.tftricks.app.ui.screens.items.ItemDetailScreen
import com.tftricks.app.ui.screens.items.ItemsScreen
import com.tftricks.app.ui.screens.more.MoreScreen
import com.tftricks.app.ui.screens.patchnotes.PatchNoteDetailScreen
import com.tftricks.app.ui.screens.patchnotes.PatchNotesScreen
import com.tftricks.app.ui.screens.saved.SavedCompsScreen
import com.tftricks.app.ui.screens.search.SearchScreen
import com.tftricks.app.ui.screens.settings.OverlaySettingsScreen
import com.tftricks.app.ui.screens.settings.SettingsScreen
import com.tftricks.app.ui.screens.traits.TraitsScreen

/** Navigate to a top-level destination keeping a single copy on the stack. */
fun NavHostController.navigateTopLevel(destination: Destination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun TFTricksNavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val openComp = { id: String -> navController.navigate(DetailRoutes.comp(id)) }
    val openChampion = { id: String -> navController.navigate(DetailRoutes.champion(id)) }
    val openItem = { id: String -> navController.navigate(DetailRoutes.item(id)) }
    val openPatch = { id: String -> navController.navigate(DetailRoutes.patch(id)) }
    val openTopLevel = { destination: Destination -> navController.navigateTopLevel(destination) }

    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier,
        // Quick cross-fades: smooth without slowing down mid-game lookups.
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(150)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(150)) }
    ) {
        composable(Destination.Home.route) {
            HomeScreen(
                contentPadding = contentPadding,
                onOpenDestination = openTopLevel,
                onOpenComp = openComp,
                onOpenChampion = openChampion,
                onOpenItem = openItem
            )
        }
        composable(Destination.TeamComps.route) {
            TeamCompsScreen(contentPadding = contentPadding, onOpenComp = openComp)
        }
        composable(Destination.Champions.route) {
            ChampionsScreen(contentPadding = contentPadding, onOpenChampion = openChampion)
        }
        composable(Destination.TeamBuilder.route) { TeamBuilderScreen(contentPadding) }
        composable(Destination.More.route) {
            MoreScreen(
                contentPadding = contentPadding,
                onNavigate = { destination -> navController.navigate(destination.route) }
            )
        }
        composable(Destination.Traits.route) {
            TraitsScreen(contentPadding = contentPadding, onOpenChampion = openChampion)
        }
        composable(Destination.Items.route) {
            ItemsScreen(contentPadding = contentPadding, onOpenItem = openItem)
        }
        composable(Destination.Augments.route) {
            AugmentsScreen(contentPadding = contentPadding, onOpenComp = openComp)
        }
        composable(Destination.SavedComps.route) {
            SavedCompsScreen(
                contentPadding = contentPadding,
                onOpenComp = openComp,
                onOpenBuilder = { openTopLevel(Destination.TeamBuilder) }
            )
        }
        composable(Destination.PatchNotes.route) {
            PatchNotesScreen(contentPadding = contentPadding, onOpenPatch = openPatch)
        }
        composable(Destination.Search.route) {
            SearchScreen(
                contentPadding = contentPadding,
                onOpenComp = openComp,
                onOpenChampion = openChampion,
                onOpenItem = openItem,
                onOpenTraits = { navController.navigate(Destination.Traits.route) },
                onOpenAugments = { navController.navigate(Destination.Augments.route) }
            )
        }
        composable(Destination.Settings.route) {
            SettingsScreen(
                contentPadding = contentPadding,
                onOpenOverlaySettings = { navController.navigate(Destination.OverlaySettings.route) }
            )
        }
        composable(Destination.OverlaySettings.route) { OverlaySettingsScreen(contentPadding) }
        composable(Destination.CrashLog.route) { CrashLogScreen(contentPadding) }

        composable(
            route = DetailRoutes.COMP_PATTERN,
            arguments = listOf(navArgument(DetailRoutes.COMP_ARG) { type = NavType.StringType })
        ) {
            CompDetailScreen(
                contentPadding = contentPadding,
                onOpenChampion = openChampion,
                onOpenItem = openItem
            )
        }
        composable(
            route = DetailRoutes.CHAMPION_PATTERN,
            arguments = listOf(navArgument(DetailRoutes.CHAMPION_ARG) { type = NavType.StringType })
        ) {
            ChampionDetailScreen(
                contentPadding = contentPadding,
                onOpenComp = openComp,
                onOpenItem = openItem
            )
        }
        composable(
            route = DetailRoutes.ITEM_PATTERN,
            arguments = listOf(navArgument(DetailRoutes.ITEM_ARG) { type = NavType.StringType })
        ) {
            ItemDetailScreen(
                contentPadding = contentPadding,
                onOpenChampion = openChampion,
                onOpenItem = openItem
            )
        }
        composable(
            route = DetailRoutes.PATCH_PATTERN,
            arguments = listOf(navArgument(DetailRoutes.PATCH_ARG) { type = NavType.StringType })
        ) {
            PatchNoteDetailScreen(contentPadding = contentPadding)
        }
    }
}
