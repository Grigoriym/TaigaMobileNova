package com.grappim.taigamobile.feature.settings.ui.user

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SettingsUserScreenNavDestination : NavKey

fun NavController.goToSettingsUserScreen() {
    navigate(route = SettingsUserScreenNavDestination)
}
