package com.grappim.taigamobile.feature.settings.ui.attributes

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object AttributesScreenNavDestination

fun NavController.goToAttributesScreen() {
    navigate(route = AttributesScreenNavDestination)
}
