package com.grappim.taigamobile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import com.grappim.kit.uikit.NativeText
import com.grappim.kit.uikit.widgets.drawer.DrawerItem
import com.grappim.kit.uikit.widgets.drawer.IconSource
import com.grappim.taigamobile.feature.projects.domain.ProjectSimple
import com.grappim.taigamobile.feature.projects.domain.canViewEpics
import com.grappim.taigamobile.feature.projects.domain.canViewIssues
import com.grappim.taigamobile.feature.projects.domain.canViewUserStories
import com.grappim.taigamobile.feature.projects.domain.canViewWikiLinks
import com.grappim.taigamobile.feature.projects.domain.canViewWikiPages
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.backlog
import com.grappim.taigamobile.strings.generated.resources.closed_sprints
import com.grappim.taigamobile.strings.generated.resources.dashboard_short
import com.grappim.taigamobile.strings.generated.resources.epics
import com.grappim.taigamobile.strings.generated.resources.issues
import com.grappim.taigamobile.strings.generated.resources.kanban
import com.grappim.taigamobile.strings.generated.resources.open_sprints
import com.grappim.taigamobile.strings.generated.resources.project_selector
import com.grappim.taigamobile.strings.generated.resources.scrum
import com.grappim.taigamobile.strings.generated.resources.settings
import com.grappim.taigamobile.strings.generated.resources.team
import com.grappim.taigamobile.strings.generated.resources.wiki
import com.grappim.taigamobile.strings.generated.resources.wiki_bookmarks
import com.grappim.taigamobile.strings.generated.resources.wiki_pages
import com.grappim.taigamobile.uikit.generated.resources.ic_dashboard
import com.grappim.taigamobile.uikit.generated.resources.ic_epics
import com.grappim.taigamobile.uikit.generated.resources.ic_folder
import com.grappim.taigamobile.uikit.generated.resources.ic_issues
import com.grappim.taigamobile.uikit.generated.resources.ic_kanban
import com.grappim.taigamobile.uikit.generated.resources.ic_settings
import com.grappim.taigamobile.uikit.generated.resources.ic_team
import com.grappim.taigamobile.uikit.generated.resources.ic_wiki
import com.grappim.taigamobile.uikit.utils.RDrawable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Factory

@Factory
class DrawerItemsBuilder {

    fun build(project: ProjectSimple): ImmutableList<DrawerItem<DrawerDestination>> {
        val permissions = project.myPermissions
        val items = mutableListOf<DrawerItem<DrawerDestination>>()

        items.add(
            DrawerItem.Destination(
                destination = DrawerDestination.ProjectSelector,
                label = NativeText.Resource(RString.project_selector),
                icon = IconSource.Resource(RDrawable.ic_folder)
            )
        )

        items.add(
            DrawerItem.Destination(
                destination = DrawerDestination.Dashboard,
                label = NativeText.Resource(RString.dashboard_short),
                icon = IconSource.Resource(RDrawable.ic_dashboard)
            )
        )

        if (permissions.canViewEpics() && project.isEpicsActivated) {
            items.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.Epics,
                    label = NativeText.Resource(RString.epics),
                    icon = IconSource.Resource(RDrawable.ic_epics)
                )
            )
        }

        if (permissions.canViewIssues() && project.isIssuesActivated) {
            items.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.Issues,
                    label = NativeText.Resource(RString.issues),
                    icon = IconSource.Resource(RDrawable.ic_issues)
                )
            )
        }

        if (permissions.canViewUserStories() && project.isKanbanActivated) {
            items.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.Kanban,
                    label = NativeText.Resource(RString.kanban),
                    icon = IconSource.Resource(RDrawable.ic_kanban)
                )
            )
        }

        items.add(
            DrawerItem.Destination(
                destination = DrawerDestination.Team,
                label = NativeText.Resource(RString.team),
                icon = IconSource.Resource(RDrawable.ic_team)
            )
        )

        if (project.isWikiActivated && (permissions.canViewWikiPages() || permissions.canViewWikiLinks())) {
            val wikiItems = mutableListOf<DrawerItem.Destination<DrawerDestination>>()

            if (permissions.canViewWikiLinks()) {
                wikiItems.add(
                    DrawerItem.Destination(
                        destination = DrawerDestination.WikiLinks,
                        label = NativeText.Resource(RString.wiki_bookmarks),
                        icon = IconSource.Vector(Icons.Default.Bookmark)
                    )
                )
            }

            if (permissions.canViewWikiPages()) {
                wikiItems.add(
                    DrawerItem.Destination(
                        destination = DrawerDestination.WikiPages,
                        label = NativeText.Resource(RString.wiki_pages),
                        icon = IconSource.Resource(RDrawable.ic_wiki)
                    )
                )
            }

            items.add(
                DrawerItem.Group(
                    label = NativeText.Resource(RString.wiki),
                    items = wikiItems
                )
            )
        }

        if (project.isBacklogActivated && permissions.canViewUserStories()) {
            val scrumItems = mutableListOf<DrawerItem.Destination<DrawerDestination>>()

            scrumItems.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.ScrumBacklog,
                    label = NativeText.Resource(RString.backlog),
                    icon = IconSource.Vector(Icons.AutoMirrored.Outlined.FormatListBulleted)
                )
            )

            scrumItems.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.ScrumOpenSprints,
                    label = NativeText.Resource(RString.open_sprints),
                    icon = IconSource.Vector(Icons.AutoMirrored.Filled.DirectionsRun)
                )
            )

            scrumItems.add(
                DrawerItem.Destination(
                    destination = DrawerDestination.ScrumClosedSprints,
                    label = NativeText.Resource(RString.closed_sprints),
                    icon = IconSource.Vector(Icons.Filled.Archive)
                )
            )

            items.add(
                DrawerItem.Group(
                    label = NativeText.Resource(RString.scrum),
                    items = scrumItems
                )
            )
        }

        items.add(DrawerItem.Divider)

        items.add(
            DrawerItem.Destination(
                destination = DrawerDestination.Settings,
                label = NativeText.Resource(RString.settings),
                icon = IconSource.Resource(RDrawable.ic_settings)
            )
        )

        return items.toImmutableList()
    }
}
