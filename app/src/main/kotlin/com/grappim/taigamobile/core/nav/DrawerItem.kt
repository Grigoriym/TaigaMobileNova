package com.grappim.taigamobile.core.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface IconSource {
    data class Vector(val imageVector: ImageVector) : IconSource
    data class Resource(@DrawableRes val resourceId: Int) : IconSource
}

sealed interface DrawerItem {
    data class Group(@StringRes val label: Int, val items: List<Destination>) : DrawerItem

    data class Destination(val destination: DrawerDestination, @StringRes val label: Int, val icon: IconSource) :
        DrawerItem {
        val route: Any get() = destination.route
    }

    data object Divider : DrawerItem
}
