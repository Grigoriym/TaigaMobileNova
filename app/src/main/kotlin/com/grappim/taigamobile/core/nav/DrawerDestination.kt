package com.grappim.taigamobile.core.nav

import KanbanNavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.epics.ui.list.EpicsNavDestination
import com.grappim.taigamobile.feature.issues.ui.list.IssuesNavDestination
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumBacklogDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumClosedSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumOpenSprintsDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.teams.ui.TeamNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiNavDestination

fun DrawerDestination.navigate(navController: NavHostController, navOptions: NavOptions) {
    navController.navigate(route = route, navOptions = navOptions)
}

enum class DrawerDestination(val route: Any) {
    ProjectSelector(ProjectSelectorNavDestination()),
    Dashboard(DashboardNavDestination),
    Epics(EpicsNavDestination),
    Issues(IssuesNavDestination),
    Kanban(KanbanNavDestination),
    Team(TeamNavDestination),
    Wiki(WikiNavDestination),
    Settings(SettingsNavDestination),
    Logout(""),
    ScrumBacklog(ScrumBacklogDestination),
    ScrumOpenSprints(ScrumOpenSprintsDestination),
    ScrumClosedSprints(ScrumClosedSprintsDestination)
}
