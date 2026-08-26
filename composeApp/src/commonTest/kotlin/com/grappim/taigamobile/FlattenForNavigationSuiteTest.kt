package com.grappim.taigamobile

import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.backlog
import com.grappim.taigamobile.strings.generated.resources.closed_sprints
import com.grappim.taigamobile.strings.generated.resources.dashboard_short
import com.grappim.taigamobile.strings.generated.resources.scrum
import com.grappim.taigamobile.strings.generated.resources.settings
import com.grappim.taigamobile.uikit.generated.resources.ic_dashboard
import com.grappim.taigamobile.uikit.generated.resources.ic_settings
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
    private val settingsItem = DrawerItem.Destination(
        destination = DrawerDestination.Settings,
        label = RString.settings,
        icon = IconSource.Resource(RDrawable.ic_settings)
    )

    @Test
    fun destinationsPassThroughUnchanged() {
        val items = persistentListOf<DrawerItem>(dashboard, settingsItem)

        assertEquals(listOf(dashboard, settingsItem), flattenForNavigationSuite(items))
    }

    @Test
    fun groupIsUnwrappedToItsInnerDestinations() {
        val group = DrawerItem.Group(label = RString.scrum, items = listOf(scrumBacklog, scrumClosedSprints))
        val items = persistentListOf<DrawerItem>(dashboard, group, settingsItem)

        assertEquals(
            listOf(dashboard, scrumBacklog, scrumClosedSprints, settingsItem),
            flattenForNavigationSuite(items)
        )
    }

    @Test
    fun dividerIsDropped() {
        val items = persistentListOf(dashboard, DrawerItem.Divider, settingsItem)

        assertEquals(listOf(dashboard, settingsItem), flattenForNavigationSuite(items))
    }
}
