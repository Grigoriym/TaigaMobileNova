package com.grappim.taigamobile.settings

import androidx.navigation.NavController
import com.grappim.taigamobile.core.nav.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object SettingsNavDestination

fun NavController.navigateToSettingsAsTopDestination() {
    navigate(route = SettingsNavDestination) {
        popUpToTop(this@navigateToSettingsAsTopDestination)
    }
}
