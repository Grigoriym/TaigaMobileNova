package com.grappim.taigamobile.feature.settings.ui.interfacescreen

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object SettingsInterfaceScreenNavDestination

fun NavController.goToSettingsInterfaceScreen() {
    navigate(route = SettingsInterfaceScreenNavDestination)
}
