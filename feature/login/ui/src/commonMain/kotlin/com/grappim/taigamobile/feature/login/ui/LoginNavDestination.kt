package com.grappim.taigamobile.feature.login.ui

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object LoginNavDestination : NavKey

fun NavController.navigateToLoginAsTopDestination() {
    navigate(route = LoginNavDestination) {
        popUpToTop(this@navigateToLoginAsTopDestination)
    }
}
