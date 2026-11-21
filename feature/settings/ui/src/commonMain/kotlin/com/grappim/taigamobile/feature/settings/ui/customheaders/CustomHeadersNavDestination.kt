package com.grappim.taigamobile.feature.settings.ui.customheaders

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object CustomHeadersNavDestination : NavKey

fun Navigator.goToCustomHeadersScreen() {
    navigate(CustomHeadersNavDestination)
}
