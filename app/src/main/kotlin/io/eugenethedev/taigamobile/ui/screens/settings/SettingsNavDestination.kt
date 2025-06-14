package io.eugenethedev.taigamobile.ui.screens.settings

import androidx.navigation.NavController
import io.eugenethedev.taigamobile.core.nav.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object SettingsNavDestination

fun NavController.navigateToSettingsAsTopDestination() {
    navigate(route = SettingsNavDestination) {
        popUpToTop(this@navigateToSettingsAsTopDestination)
    }
}
