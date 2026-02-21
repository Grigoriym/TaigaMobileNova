package com.grappim.taigamobile.feature.dashboard.ui

import androidx.navigation.NavController
import kotlinx.serialization.Serializable
import popUpToTop

@Serializable
data object DashboardNavDestination

fun NavController.navigateToDashboardAsTopDestination() {
    navigate(route = DashboardNavDestination) {
        popUpToTop(this@navigateToDashboardAsTopDestination)
    }
}
