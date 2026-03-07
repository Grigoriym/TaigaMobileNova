package com.grappim.taigamobile.feature.settings.ui.modules

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
object ModulesNavDestination

fun NavController.navigateToModules() {
    navigate(ModulesNavDestination)
}
