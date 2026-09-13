package com.grappim.taigamobile

import com.grappim.kit.uikit.NativeText
import com.grappim.kit.uikit.widgets.drawer.DrawerItem
import com.grappim.kit.uikit.widgets.drawer.IconSource
import com.grappim.kit.uikit.widgets.drawer.flattenForNavigationSuite
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
        label = NativeText.Resource(RString.dashboard_short),
        icon = IconSource.Resource(RDrawable.ic_dashboard)
    )
    private val scrumBacklog = DrawerItem.Destination(
        destination = DrawerDestination.ScrumBacklog,
        label = NativeText.Resource(RString.backlog),
        icon = IconSource.Resource(RDrawable.ic_dashboard)
    )
    private val scrumClosedSprints = DrawerItem.Destination(
        destination = DrawerDestination.ScrumClosedSprints,
        label = NativeText.Resource(RString.closed_sprints),
        icon = IconSource.Resource(RDrawable.ic_dashboard)
    )
    private val settingsItem = DrawerItem.Destination(
        destination = DrawerDestination.Settings,
        label = NativeText.Resource(RString.settings),
        icon = IconSource.Resource(RDrawable.ic_settings)
    )

    @Test
    fun destinationsPassThroughUnchanged() {
        val items = persistentListOf<DrawerItem<DrawerDestination>>(dashboard, settingsItem)

        assertEquals(listOf(dashboard, settingsItem), flattenForNavigationSuite(items))
    }

    @Test
    fun groupIsUnwrappedToItsInnerDestinations() {
        val group = DrawerItem.Group(
            label = NativeText.Resource(RString.scrum),
            items = listOf(scrumBacklog, scrumClosedSprints)
        )
        val items = persistentListOf<DrawerItem<DrawerDestination>>(dashboard, group, settingsItem)

        assertEquals(
            listOf(dashboard, scrumBacklog, scrumClosedSprints, settingsItem),
            flattenForNavigationSuite(items)
        )
    }

    @Test
    fun dividerIsDropped() {
        val items = persistentListOf<DrawerItem<DrawerDestination>>(dashboard, DrawerItem.Divider, settingsItem)

        assertEquals(listOf(dashboard, settingsItem), flattenForNavigationSuite(items))
    }
}
