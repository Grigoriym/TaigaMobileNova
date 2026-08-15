package com.grappim.taigamobile.feature.settings.ui.interfacescreen

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SettingsInterfaceScreenNavDestination : NavKey

fun NavController.goToSettingsInterfaceScreen() {
    navigate(route = SettingsInterfaceScreenNavDestination)
}
