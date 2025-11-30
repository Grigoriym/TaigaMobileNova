package com.grappim.taigamobile.core.nav

import KanbanNavDestination
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.epics.ui.list.EpicsNavDestination
import com.grappim.taigamobile.feature.issues.ui.list.IssuesNavDestination
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumNavDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.teams.ui.TeamNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiNavDestination
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable

fun DrawerDestination.navigate(navController: NavHostController, navOptions: NavOptions) {
    navController.navigate(route = route, navOptions = navOptions)
}

enum class DrawerDestination(val route: Any, @StringRes val label: Int, @DrawableRes val icon: Int) {
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
    ),
    Logout(
        "",
        RString.logout_title,
        RDrawable.ic_logout
    )
}
