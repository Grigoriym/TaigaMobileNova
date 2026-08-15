package com.grappim.taigamobile.feature.settings.ui.attributes.tags

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object TagsScreenRouteNavDestination : NavKey

fun NavController.goToTagsScreen() {
    navigate(route = TagsScreenRouteNavDestination)
}
