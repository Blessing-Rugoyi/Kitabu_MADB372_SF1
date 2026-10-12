package com.stadiolinks.kitabu.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.stadiolinks.kitabu.R

/** Top-level destinations shown in the bottom navigation bar. */
sealed class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    data object Catalog : Destination("catalog", R.string.nav_catalog, Icons.Default.Home)
    data object Reservations : Destination("reservations", R.string.nav_reservations, Icons.Default.DateRange)
}

/**
 * Bottom-bar tabs. Kept at top level (not in a companion inside [Destination]) so that the
 * sealed class never has to initialise its own subclasses while it is still initialising.
 */
val bottomBarDestinations: List<Destination> = listOf(Destination.Catalog, Destination.Reservations)

/** Standard bottom-bar navigation: one copy of each tab, state saved and restored. */
fun NavController.navigateToTopLevel(destination: Destination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
