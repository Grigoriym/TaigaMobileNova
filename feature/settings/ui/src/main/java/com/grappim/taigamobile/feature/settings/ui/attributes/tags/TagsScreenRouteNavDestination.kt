package com.grappim.taigamobile.feature.settings.ui.attributes.tags

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object TagsScreenRouteNavDestination

fun NavController.goToTagsScreen() {
    navigate(route = TagsScreenRouteNavDestination)
}
