package com.grappim.taigamobile.feature.settings.ui.about

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object SettingsAboutScreenRouteNavDestination

fun NavController.goToSettingsAboutScreen() {
    navigate(route = SettingsAboutScreenRouteNavDestination)
}
