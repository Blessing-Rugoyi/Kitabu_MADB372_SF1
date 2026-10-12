package com.stadiolinks.kitabu.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stadiolinks.kitabu.ui.catalog.CatalogRoute
import com.stadiolinks.kitabu.ui.navigation.Destination
import com.stadiolinks.kitabu.ui.navigation.bottomBarDestinations
import com.stadiolinks.kitabu.ui.navigation.navigateToTopLevel
import com.stadiolinks.kitabu.ui.reservations.ReservationsRoute

/** App shell: bottom navigation, snackbar host and the single NavHost. */
@Composable
fun KitabuApp(navController: NavHostController = rememberNavController()) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { KitabuBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Catalog.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Catalog.route) {
                CatalogRoute(snackbarHostState = snackbarHostState)
            }
            composable(Destination.Reservations.route) {
                ReservationsRoute(
                    snackbarHostState = snackbarHostState,
                    onBrowseCatalog = { navController.navigateToTopLevel(Destination.Catalog) }
                )
            }
        }
    }
}

@Composable
private fun KitabuBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        bottomBarDestinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTopLevel(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                label = { Text(text = stringResource(destination.labelRes)) }
            )
        }
    }
}
