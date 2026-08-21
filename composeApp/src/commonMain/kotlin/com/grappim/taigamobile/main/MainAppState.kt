package com.grappim.taigamobile.main

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.DrawerDestination
import com.grappim.taigamobile.core.navigation.Navigator
import com.grappim.taigamobile.core.navigation.rememberNavigationState
import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.epics.ui.list.EpicsNavDestination
import com.grappim.taigamobile.feature.issues.ui.list.IssuesNavDestination
import com.grappim.taigamobile.feature.kanban.ui.KanbanNavDestination
import com.grappim.taigamobile.feature.login.ui.LoginNavDestination
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumBacklogDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumClosedSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumOpenSprintsDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.teams.ui.TeamNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiLinksNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPagesNavDestination
import com.grappim.taigamobile.nav.navSavedStateConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Every top-level drawer section gets its own [com.grappim.taigamobile.core.navigation.NavigationState]
 * sub-stack, plus [LoginNavDestination] (not a drawer destination, but needs a slot too since
 * [NavigationState.currentSubStack] requires the active top-level key to resolve one).
 */
private val TOP_LEVEL_KEYS: Set<NavKey> = setOf(
    LoginNavDestination,
    ProjectSelectorNavDestination(),
    DashboardNavDestination,
    EpicsNavDestination,
    IssuesNavDestination,
    KanbanNavDestination,
    TeamNavDestination,
    WikiPagesNavDestination,
    WikiLinksNavDestination,
    SettingsNavDestination,
    ScrumBacklogDestination,
    ScrumOpenSprintsDestination,
    ScrumClosedSprintsDestination
)

@Composable
fun rememberMainAppState(): MainAppState {
    val navigationState = rememberNavigationState(
        startKey = LoginNavDestination,
        topLevelKeys = TOP_LEVEL_KEYS,
        configuration = navSavedStateConfiguration
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    return remember(navigator) { MainAppState(navigator) }
}

/**
 * Responsible for holding state related to navigation-wide data.
 */
@Stable
class MainAppState(val navigator: Navigator) {

    private val navigationState get() = navigator.state

    private val _drawerState = MutableStateFlow(DrawerState(initialValue = DrawerValue.Closed))
    val drawerState = _drawerState.asStateFlow()

    val currentTopLevelDestination: DrawerDestination?
        @Composable get() {
            val currentTopLevelKey = navigationState.currentTopLevelKey
            return DrawerDestination.entries.firstOrNull { drawerDestination ->
                drawerDestination.route::class == currentTopLevelKey::class
            }
        }

    val areDrawerGesturesEnabled: Boolean
        @Composable get() = currentTopLevelDestination != null

    val isTopBarVisible: Boolean
        @Composable get() = navigationState.currentKey::class != LoginNavDestination::class

    fun navigateToTopLevelDestination(destination: DrawerDestination) {
        navigator.navigate(destination.route as NavKey)
    }
}
