package com.grappim.taigamobile.login

import androidx.navigation.NavController
import com.grappim.taigamobile.core.nav.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object LoginNavDestination

fun NavController.navigateToLoginAsTopDestination() {
    navigate(route = LoginNavDestination) {
        popUpToTop(this@navigateToLoginAsTopDestination)
    }
}
