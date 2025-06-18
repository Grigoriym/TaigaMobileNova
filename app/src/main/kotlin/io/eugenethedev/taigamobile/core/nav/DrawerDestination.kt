package io.eugenethedev.taigamobile.core.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.dashboard.DashboardNavDestination
import io.eugenethedev.taigamobile.epics.EpicsNavDestination
import io.eugenethedev.taigamobile.issues.IssuesNavDestination
import io.eugenethedev.taigamobile.kanban.KanbanNavDestination
import io.eugenethedev.taigamobile.projectselector.ProjectSelectorNavDestination
import io.eugenethedev.taigamobile.scrum.ScrumNavDestination
import io.eugenethedev.taigamobile.settings.SettingsNavDestination
import io.eugenethedev.taigamobile.team.TeamNavDestination
import io.eugenethedev.taigamobile.wiki.WikiNavDestination

fun DrawerDestination.navigate(navController: NavHostController, navOptions: NavOptions) {
    navController.navigate(route = route, navOptions)
}

enum class DrawerDestination(
    val route: Any,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    ProjectSelector(
        ProjectSelectorNavDestination(),
        R.string.project_selector,
        R.drawable.ic_folder,
    ),
    Dashboard(
        DashboardNavDestination,
        R.string.dashboard_short,
        R.drawable.ic_dashboard,
    ),
    Scrum(
        ScrumNavDestination, R.string.scrum, R.drawable.ic_scrum,
    ),
    Epics(
        EpicsNavDestination, R.string.epics, R.drawable.ic_epics,
    ),
    Issues(
        IssuesNavDestination, R.string.issues, R.drawable.ic_issues,
    ),
    Kanban(
        KanbanNavDestination, R.string.kanban, R.drawable.ic_kanban,
    ),
    Team(
        TeamNavDestination, R.string.team, R.drawable.ic_team,
    ),
    Wiki(
        WikiNavDestination, R.string.wiki, R.drawable.ic_wiki,
    ),
    Settings(
        SettingsNavDestination, R.string.settings, R.drawable.ic_settings,
    ),
}
