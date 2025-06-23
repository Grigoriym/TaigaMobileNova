package com.grappim.taigamobile.main

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.grappim.taigamobile.core.nav.DrawerDestination
import com.grappim.taigamobile.core.nav.navigate
import com.grappim.taigamobile.feature.login.ui.LoginNavDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.reflect.KClass

@Composable
fun rememberMainAppState(navController: NavHostController = rememberNavController()): MainAppState =
    remember(navController) {
        MainAppState(navController)
    }

/**
 * Responsible for holding state related to navigation-wide data.
 */
@Stable
class MainAppState(val navController: NavHostController) {

    private val previousDestination = mutableStateOf<NavDestination?>(null)

    private val _drawerState = MutableStateFlow(DrawerState(initialValue = DrawerValue.Closed))
    val drawerState = _drawerState.asStateFlow()

    private val currentDestination: NavDestination?
        @Composable get() {
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val topLevelDestinations = DrawerDestination.entries

    val currentTopLevelDestination: DrawerDestination?
        @Composable get() = DrawerDestination.entries.firstOrNull { drawerDestination ->
            currentDestination?.hasRoute(route = drawerDestination.route::class) == true
        }

    val areDrawerGesturesEnabled: Boolean
        @Composable get() = currentTopLevelDestination != null

    private val destinationsWithoutTopBar = setOf<KClass<out Any>>(
        LoginNavDestination::class
    )

    val isTopBarVisible: Boolean
        @Composable get() = destinationsWithoutTopBar.firstOrNull { dest ->
            currentDestination?.hasRoute(route = dest) == true
        } == null

    fun navigateToTopLevelDestination(destination: DrawerDestination) {
        val navOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }

        destination.navigate(navController, navOptions)
    }
}
