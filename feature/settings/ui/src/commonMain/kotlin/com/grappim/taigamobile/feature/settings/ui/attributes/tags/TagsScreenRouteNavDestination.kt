package com.grappim.taigamobile.feature.settings.ui.attributes.tags

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object TagsScreenRouteNavDestination : NavKey

fun Navigator.goToTagsScreen() {
    navigate(TagsScreenRouteNavDestination)
}
