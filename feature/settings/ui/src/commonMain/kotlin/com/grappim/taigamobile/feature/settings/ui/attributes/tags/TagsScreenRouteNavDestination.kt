package com.grappim.taigamobile.feature.settings.ui.attributes.tags

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object TagsScreenRouteNavDestination : NavKey

fun Navigator.goToTagsScreen() {
    navigate(TagsScreenRouteNavDestination)
}
