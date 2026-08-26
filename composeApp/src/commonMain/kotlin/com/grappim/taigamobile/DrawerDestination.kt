package com.grappim.taigamobile

import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.epics.ui.list.EpicsNavDestination
import com.grappim.taigamobile.feature.issues.ui.list.IssuesNavDestination
import com.grappim.taigamobile.feature.kanban.ui.KanbanNavDestination
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumBacklogDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumClosedSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumOpenSprintsDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.teams.ui.TeamNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiLinksNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPagesNavDestination

enum class DrawerDestination(val route: Any) {
    ProjectSelector(ProjectSelectorNavDestination()),
    Dashboard(DashboardNavDestination),
    Epics(EpicsNavDestination),
    Issues(IssuesNavDestination),
    Kanban(KanbanNavDestination),
    Team(TeamNavDestination),
    Wiki(WikiNavDestination),
    WikiPages(WikiPagesNavDestination),
    WikiLinks(WikiLinksNavDestination),
    Settings(SettingsNavDestination),
    ScrumBacklog(ScrumBacklogDestination),
    ScrumOpenSprints(ScrumOpenSprintsDestination),
    ScrumClosedSprints(ScrumClosedSprintsDestination)
}
