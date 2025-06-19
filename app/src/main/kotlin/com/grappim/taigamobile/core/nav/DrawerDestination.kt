package com.grappim.taigamobile.core.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.grappim.taigamobile.dashboard.DashboardNavDestination
import com.grappim.taigamobile.epics.EpicsNavDestination
import com.grappim.taigamobile.issues.IssuesNavDestination
import com.grappim.taigamobile.kanban.KanbanNavDestination
import com.grappim.taigamobile.projectselector.ProjectSelectorNavDestination
import com.grappim.taigamobile.scrum.ScrumNavDestination
import com.grappim.taigamobile.settings.SettingsNavDestination
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.team.TeamNavDestination
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.wiki.WikiNavDestination

fun DrawerDestination.navigate(navController: NavHostController, navOptions: NavOptions) {
    navController.navigate(route = route, navOptions = navOptions)
}

enum class DrawerDestination(
    val route: Any,
    @StringRes val label: Int,
    @DrawableRes val icon: Int
) {
    ProjectSelector(
        ProjectSelectorNavDestination(),
        RString.project_selector,
        RDrawable.ic_folder
    ),
    Dashboard(
        DashboardNavDestination,
        RString.dashboard_short,
        RDrawable.ic_dashboard
    ),
    Scrum(
        ScrumNavDestination,
        RString.scrum,
        RDrawable.ic_scrum
    ),
    Epics(
        EpicsNavDestination,
        RString.epics,
        RDrawable.ic_epics
    ),
    Issues(
        IssuesNavDestination,
        RString.issues,
        RDrawable.ic_issues
    ),
    Kanban(
        KanbanNavDestination,
        RString.kanban,
        RDrawable.ic_kanban
    ),
    Team(
        TeamNavDestination,
        RString.team,
        RDrawable.ic_team
    ),
    Wiki(
        WikiNavDestination,
        RString.wiki,
        RDrawable.ic_wiki
    ),
    Settings(
        SettingsNavDestination,
        RString.settings,
        RDrawable.ic_settings
    )
}
