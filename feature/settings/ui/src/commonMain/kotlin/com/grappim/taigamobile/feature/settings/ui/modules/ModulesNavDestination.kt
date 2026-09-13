package com.grappim.taigamobile.feature.settings.ui.modules

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object ModulesNavDestination : NavKey

fun Navigator.navigateToModules() {
    navigate(ModulesNavDestination)
}
