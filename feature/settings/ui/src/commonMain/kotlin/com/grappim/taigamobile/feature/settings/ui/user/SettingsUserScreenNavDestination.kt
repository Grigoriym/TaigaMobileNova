package com.grappim.taigamobile.feature.settings.ui.user

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object SettingsUserScreenNavDestination

fun NavController.goToSettingsUserScreen() {
    navigate(route = SettingsUserScreenNavDestination)
}
