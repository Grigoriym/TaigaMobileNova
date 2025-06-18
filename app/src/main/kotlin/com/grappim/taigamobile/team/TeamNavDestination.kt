package com.grappim.taigamobile.team

import androidx.navigation.NavController
import com.grappim.taigamobile.core.nav.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object TeamNavDestination

fun NavController.navigateToTeamAsTopDestination() {
    navigate(route = TeamNavDestination) {
        popUpToTop(this@navigateToTeamAsTopDestination)
    }
}
