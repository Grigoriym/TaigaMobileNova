package com.grappim.taigamobile.feature.settings.ui.modules

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object ModulesNavDestination : NavKey

fun NavController.navigateToModules() {
    navigate(ModulesNavDestination)
}
