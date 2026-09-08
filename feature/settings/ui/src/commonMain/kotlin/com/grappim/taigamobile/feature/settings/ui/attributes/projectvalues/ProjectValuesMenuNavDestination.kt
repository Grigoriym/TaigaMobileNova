package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object ProjectValuesMenuNavDestination : NavKey

fun Navigator.navigateToProjectValuesMenu() {
    navigate(ProjectValuesMenuNavDestination)
}
