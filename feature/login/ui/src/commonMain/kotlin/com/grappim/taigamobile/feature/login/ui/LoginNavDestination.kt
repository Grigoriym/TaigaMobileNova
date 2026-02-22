package com.grappim.taigamobile.feature.login.ui

import androidx.navigation.NavController
import com.grappim.taigamobile.core.navigation.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object LoginNavDestination

fun NavController.navigateToLoginAsTopDestination() {
    navigate(route = LoginNavDestination) {
        popUpToTop(this@navigateToLoginAsTopDestination)
    }
}
