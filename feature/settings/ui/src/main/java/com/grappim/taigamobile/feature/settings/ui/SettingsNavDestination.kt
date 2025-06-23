package com.grappim.taigamobile.feature.settings.ui

import androidx.navigation.NavController
import com.grappim.taigamobile.core.navigation.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object SettingsNavDestination

fun NavController.navigateToSettingsAsTopDestination() {
    navigate(route = SettingsNavDestination) {
        popUpToTop(this@navigateToSettingsAsTopDestination)
    }
}
