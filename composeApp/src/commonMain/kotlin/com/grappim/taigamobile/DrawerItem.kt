package com.grappim.taigamobile

import androidx.compose.ui.graphics.vector.ImageVector
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
