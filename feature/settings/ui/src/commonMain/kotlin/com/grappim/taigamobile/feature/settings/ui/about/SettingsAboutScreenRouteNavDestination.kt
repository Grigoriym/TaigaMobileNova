package com.grappim.taigamobile.feature.settings.ui.about

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object SettingsAboutScreenRouteNavDestination : NavKey

fun Navigator.goToSettingsAboutScreen() {
    navigate(SettingsAboutScreenRouteNavDestination)
}
