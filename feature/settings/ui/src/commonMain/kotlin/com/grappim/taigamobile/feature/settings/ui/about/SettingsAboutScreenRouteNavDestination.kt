package com.grappim.taigamobile.feature.settings.ui.about

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SettingsAboutScreenRouteNavDestination : NavKey

fun NavController.goToSettingsAboutScreen() {
    navigate(route = SettingsAboutScreenRouteNavDestination)
}
