package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object ProjectValuesMenuNavDestination : NavKey

fun Navigator.navigateToProjectValuesMenu() {
    navigate(ProjectValuesMenuNavDestination)
}
