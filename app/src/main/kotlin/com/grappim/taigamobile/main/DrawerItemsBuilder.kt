package com.grappim.taigamobile.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import com.grappim.taigamobile.core.nav.DrawerDestination
import com.grappim.taigamobile.core.nav.DrawerItem
import com.grappim.taigamobile.core.nav.IconSource
import com.grappim.taigamobile.feature.projects.domain.ProjectSimple
import com.grappim.taigamobile.feature.projects.domain.canViewEpics
import com.grappim.taigamobile.feature.projects.domain.canViewIssues
import com.grappim.taigamobile.feature.projects.domain.canViewUserStories
import com.grappim.taigamobile.feature.projects.domain.canViewWikiLinks
import com.grappim.taigamobile.feature.projects.domain.canViewWikiPages
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class DrawerItemsBuilder @Inject constructor() {

    fun build(project: ProjectSimple): ImmutableList<DrawerItem> {
        val permissions = project.myPermissions
        val items = mutableListOf<DrawerItem>()

        items.add(
            DrawerItem.Destination(
                destination = DrawerDestination.ProjectSelector,
                label = RString.project_selector,
                icon = IconSource.Resource(RDrawable.ic_folder)
            )
        )

        items.add(
            DrawerItem.Destination(
                destination = DrawerDestination.Dashboard,
                label = RString.dashboard_short,
                icon = IconSource.Resource(RDrawable.ic_dashboard)
            )
        )

        if (permissions.canViewEpics() && project.isEpicsActivated) {
            items.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.Epics,
                    label = RString.epics,
                    icon = IconSource.Resource(RDrawable.ic_epics)
                )
            )
        }

        if (permissions.canViewIssues() && project.isIssuesActivated) {
            items.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.Issues,
                    label = RString.issues,
                    icon = IconSource.Resource(RDrawable.ic_issues)
                )
            )
        }

        if (permissions.canViewUserStories() && project.isKanbanActivated) {
            items.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.Kanban,
                    label = RString.kanban,
                    icon = IconSource.Resource(RDrawable.ic_kanban)
                )
            )
        }

        items.add(
            DrawerItem.Destination(
                destination = DrawerDestination.Team,
                label = RString.team,
                icon = IconSource.Resource(RDrawable.ic_team)
            )
        )

        if (project.isWikiActivated && (permissions.canViewWikiPages() || permissions.canViewWikiLinks())) {
            val wikiItems = mutableListOf<DrawerItem.Destination>()

            if (permissions.canViewWikiLinks()) {
                wikiItems.add(
                    DrawerItem.Destination(
                        destination = DrawerDestination.WikiLinks,
                        label = RString.wiki_bookmarks,
                        icon = IconSource.Vector(Icons.Default.Bookmark)
                    )
                )
            }

            if (permissions.canViewWikiPages()) {
                wikiItems.add(
                    DrawerItem.Destination(
                        destination = DrawerDestination.WikiPages,
                        label = RString.wiki_pages,
                        icon = IconSource.Resource(RDrawable.ic_wiki)
                    )
                )
            }

            items.add(
                DrawerItem.Group(
                    label = RString.wiki,
                    items = wikiItems
                )
            )
        }

        if (project.isBacklogActivated && permissions.canViewUserStories()) {
            val scrumItems = mutableListOf<DrawerItem.Destination>()

            scrumItems.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.ScrumBacklog,
                    label = RString.backlog,
                    icon = IconSource.Vector(Icons.AutoMirrored.Outlined.FormatListBulleted)
                )
            )

            scrumItems.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.ScrumOpenSprints,
                    label = RString.open_sprints,
                    icon = IconSource.Vector(Icons.AutoMirrored.Filled.DirectionsRun)
                )
            )

            scrumItems.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.ScrumClosedSprints,
                    label = RString.closed_sprints,
                    icon = IconSource.Vector(Icons.Filled.Archive)
                )
            )

            items.add(
                DrawerItem.Group(
                    label = RString.scrum,
                    items = scrumItems
                )
            )
        }

        items.add(DrawerItem.Divider)

        items.add(
            DrawerItem.Destination(
                destination = DrawerDestination.Settings,
                label = RString.settings,
                icon = IconSource.Resource(RDrawable.ic_settings)
            )
        )

        items.add(
            DrawerItem.Destination(
                destination = DrawerDestination.Logout,
                label = RString.logout_title,
                icon = IconSource.Resource(RDrawable.ic_logout)
            )
        )

        return items.toImmutableList()
    }
}
