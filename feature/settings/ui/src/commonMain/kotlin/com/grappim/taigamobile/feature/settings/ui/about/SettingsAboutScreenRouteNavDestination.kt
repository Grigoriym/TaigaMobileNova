package com.grappim.taigamobile.feature.settings.ui.about

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object SettingsAboutScreenRouteNavDestination : NavKey

fun Navigator.goToSettingsAboutScreen() {
    navigate(SettingsAboutScreenRouteNavDestination)
}
