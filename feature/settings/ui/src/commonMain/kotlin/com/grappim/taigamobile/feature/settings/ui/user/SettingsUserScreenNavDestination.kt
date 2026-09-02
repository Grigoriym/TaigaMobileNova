package com.grappim.taigamobile.feature.settings.ui.user

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object SettingsUserScreenNavDestination : NavKey

fun Navigator.goToSettingsUserScreen() {
    navigate(SettingsUserScreenNavDestination)
}
