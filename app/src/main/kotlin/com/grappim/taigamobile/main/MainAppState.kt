package com.grappim.taigamobile.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.outlined.FormatListBulleted
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
import com.grappim.taigamobile.core.nav.DrawerItem
import com.grappim.taigamobile.core.nav.IconSource
import com.grappim.taigamobile.core.nav.navigate
import com.grappim.taigamobile.feature.login.ui.LoginNavDestination
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import kotlinx.collections.immutable.persistentListOf
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

    val drawerItems = persistentListOf(
        DrawerItem.Destination(
            destination = DrawerDestination.ProjectSelector,
            label = RString.project_selector,
            icon = IconSource.Resource(RDrawable.ic_folder)
        ),
        DrawerItem.Destination(
            destination = DrawerDestination.Dashboard,
            label = RString.dashboard_short,
            icon = IconSource.Resource(RDrawable.ic_dashboard)
        ),
        DrawerItem.Destination(
            destination = DrawerDestination.Epics,
            label = RString.epics,
            icon = IconSource.Resource(RDrawable.ic_epics)
        ),
        DrawerItem.Destination(
            destination = DrawerDestination.Issues,
            label = RString.issues,
            icon = IconSource.Resource(RDrawable.ic_issues)
        ),
        DrawerItem.Destination(
            destination = DrawerDestination.Kanban,
            label = RString.kanban,
            icon = IconSource.Resource(RDrawable.ic_kanban)
        ),
        DrawerItem.Destination(
            destination = DrawerDestination.Team,
            label = RString.team,
            icon = IconSource.Resource(RDrawable.ic_team)
        ),
        DrawerItem.Destination(
            destination = DrawerDestination.Wiki,
            label = RString.wiki,
            icon = IconSource.Resource(RDrawable.ic_wiki)
        ),
        DrawerItem.Group(
            label = RString.scrum,
            items = listOf(
                DrawerItem.Destination(
                    destination = DrawerDestination.ScrumBacklog,
                    label = RString.backlog,
                    icon = IconSource.Vector(Icons.AutoMirrored.Outlined.FormatListBulleted)
                ),
                DrawerItem.Destination(
                    destination = DrawerDestination.ScrumOpenSprints,
                    label = RString.open_sprints,
                    icon = IconSource.Vector(Icons.AutoMirrored.Filled.DirectionsRun)
                ),
                DrawerItem.Destination(
                    destination = DrawerDestination.ScrumClosedSprints,
                    label = RString.closed_sprints,
                    icon = IconSource.Vector(Icons.Filled.Archive)
                )
            )
        ),
        DrawerItem.Divider,
        DrawerItem.Destination(
            destination = DrawerDestination.Settings,
            label = RString.settings,
            icon = IconSource.Resource(RDrawable.ic_settings)
        ),
        DrawerItem.Destination(
            destination = DrawerDestination.Logout,
            label = RString.logout_title,
            icon = IconSource.Resource(RDrawable.ic_logout)
        )
    )

    val currentNavDestination: NavDestination?
        @Composable get() = currentDestination

    val currentRoute: Any?
        @Composable get() = currentDestination?.route

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
