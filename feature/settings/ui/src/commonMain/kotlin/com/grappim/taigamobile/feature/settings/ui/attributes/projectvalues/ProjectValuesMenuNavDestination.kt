package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object ProjectValuesMenuNavDestination

fun NavController.navigateToProjectValuesMenu() {
    navigate(route = ProjectValuesMenuNavDestination)
}
