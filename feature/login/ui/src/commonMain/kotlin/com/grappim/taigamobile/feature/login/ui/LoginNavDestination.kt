package com.grappim.taigamobile.feature.login.ui

import androidx.navigation.NavController
import kotlinx.serialization.Serializable
import popUpToTop

@Serializable
data object LoginNavDestination

fun NavController.navigateToLoginAsTopDestination() {
    navigate(route = LoginNavDestination) {
        popUpToTop(this@navigateToLoginAsTopDestination)
    }
}
