package com.grappim.taigamobile.feature.settings.ui.attributes

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AttributesScreenNavDestination : NavKey

fun NavController.goToAttributesScreen() {
    navigate(route = AttributesScreenNavDestination)
}
