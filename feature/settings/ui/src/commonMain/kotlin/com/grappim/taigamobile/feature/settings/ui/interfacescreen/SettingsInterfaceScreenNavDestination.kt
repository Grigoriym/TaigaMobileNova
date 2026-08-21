package com.grappim.taigamobile.feature.settings.ui.interfacescreen

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object SettingsInterfaceScreenNavDestination : NavKey

fun Navigator.goToSettingsInterfaceScreen() {
    navigate(SettingsInterfaceScreenNavDestination)
}
