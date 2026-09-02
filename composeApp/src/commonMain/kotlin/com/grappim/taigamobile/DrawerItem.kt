package com.grappim.taigamobile

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed interface IconSource {
    data class Vector(val imageVector: ImageVector) : IconSource
    data class Resource(val resourceId: DrawableResource) : IconSource
}

sealed interface DrawerItem {
    data class Group(val label: StringResource, val items: List<Destination>) : DrawerItem

    data class Destination(val destination: DrawerDestination, val label: StringResource, val icon: IconSource) :
        DrawerItem {
        val route: Any get() = destination.route
    }

    data object Divider : DrawerItem
}

/**
 * Flattens grouped drawer items for [androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold],
 * whose flat `item()` API has no header/group-label/divider slot: [DrawerItem.Group] is unwrapped to its inner
 * destinations, [DrawerItem.Divider] is dropped, item order is otherwise preserved.
 */
fun flattenForNavigationSuite(items: ImmutableList<DrawerItem>): List<DrawerItem.Destination> = items.flatMap { item ->
    when (item) {
        is DrawerItem.Destination -> listOf(item)
        is DrawerItem.Group -> item.items
        is DrawerItem.Divider -> emptyList()
    }
}
