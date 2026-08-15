package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ProjectValuesMenuNavDestination : NavKey

fun NavController.navigateToProjectValuesMenu() {
    navigate(route = ProjectValuesMenuNavDestination)
}
