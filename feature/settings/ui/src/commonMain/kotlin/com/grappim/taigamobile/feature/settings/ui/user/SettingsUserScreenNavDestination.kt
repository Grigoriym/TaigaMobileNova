package com.grappim.taigamobile.feature.settings.ui.user

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object SettingsUserScreenNavDestination : NavKey

fun Navigator.goToSettingsUserScreen() {
    navigate(SettingsUserScreenNavDestination)
}
