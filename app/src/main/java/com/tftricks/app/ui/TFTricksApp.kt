package com.tftricks.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tftricks.app.ui.navigation.Destination
import com.tftricks.app.ui.navigation.DetailRoutes
import com.tftricks.app.ui.navigation.TFTricksNavGraph
import com.tftricks.app.ui.navigation.navigateTopLevel
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.SurfaceCard
import com.tftricks.app.ui.theme.TextSecondary

/** Root scaffold: top bar (with back arrow on detail screens) + bottom navigation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TFTricksApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = Destination.fromRoute(currentRoute)
    val detailTitle = DetailRoutes.titleFor(currentRoute)

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detailTitle ?: currentDestination?.title ?: "TFTricks",
                        style = MaterialTheme.typography.titleLarge,
                        color = BrandYellow
                    )
                },
                navigationIcon = {
                    if (detailTitle != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = BrandYellow
                            )
                        }
                    }
                },
                actions = {
                    if (currentDestination != Destination.Search) {
                        IconButton(onClick = { navController.navigate(Destination.Search.route) }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack,
                    titleContentColor = BrandYellow
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceCard) {
                Destination.bottomBarDestinations.forEach { destination ->
                    val selected = when (destination) {
                        // Keep "More" highlighted while browsing its sub-screens.
                        Destination.More ->
                            currentDestination == Destination.More ||
                                currentDestination in Destination.moreDestinations
                        else -> currentDestination == destination
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigateTopLevel(destination) },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureBlack,
                            selectedTextColor = BrandYellow,
                            indicatorColor = BrandYellow,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        TFTricksNavGraph(
            navController = navController,
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        )
    }
}
