package com.grappim.taigamobile.feature.teams.ui

import androidx.navigation.NavController
import com.grappim.taigamobile.core.navigation.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object TeamNavDestination

fun NavController.navigateToTeamAsTopDestination() {
    navigate(route = TeamNavDestination) {
        popUpToTop(this@navigateToTeamAsTopDestination)
    }
}
