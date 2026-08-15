package com.grappim.taigamobile

import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.backlog
import com.grappim.taigamobile.strings.generated.resources.closed_sprints
import com.grappim.taigamobile.strings.generated.resources.dashboard_short
import com.grappim.taigamobile.strings.generated.resources.logout_title
import com.grappim.taigamobile.strings.generated.resources.scrum
import com.grappim.taigamobile.uikit.generated.resources.ic_dashboard
import com.grappim.taigamobile.uikit.generated.resources.ic_logout
import com.grappim.taigamobile.uikit.utils.RDrawable
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class FlattenForNavigationSuiteTest {

    private val dashboard = DrawerItem.Destination(
        destination = DrawerDestination.Dashboard,
        label = RString.dashboard_short,
        icon = IconSource.Resource(RDrawable.ic_dashboard)
    )
    private val scrumBacklog = DrawerItem.Destination(
        destination = DrawerDestination.ScrumBacklog,
        label = RString.backlog,
        icon = IconSource.Resource(RDrawable.ic_dashboard)
    )
    private val scrumClosedSprints = DrawerItem.Destination(
        destination = DrawerDestination.ScrumClosedSprints,
        label = RString.closed_sprints,
        icon = IconSource.Resource(RDrawable.ic_dashboard)
    )
    private val logout = DrawerItem.Destination(
        destination = DrawerDestination.Logout,
        label = RString.logout_title,
        icon = IconSource.Resource(RDrawable.ic_logout)
    )

    @Test
    fun destinationsPassThroughUnchanged() {
        val items = persistentListOf<DrawerItem>(dashboard, logout)

        assertEquals(listOf(dashboard, logout), flattenForNavigationSuite(items))
    }

    @Test
    fun groupIsUnwrappedToItsInnerDestinations() {
        val group = DrawerItem.Group(label = RString.scrum, items = listOf(scrumBacklog, scrumClosedSprints))
        val items = persistentListOf<DrawerItem>(dashboard, group, logout)

        assertEquals(
            listOf(dashboard, scrumBacklog, scrumClosedSprints, logout),
            flattenForNavigationSuite(items)
        )
    }

    @Test
    fun dividerIsDropped() {
        val items = persistentListOf(dashboard, DrawerItem.Divider, logout)

        assertEquals(listOf(dashboard, logout), flattenForNavigationSuite(items))
    }
}
