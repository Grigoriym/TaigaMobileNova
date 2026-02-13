package com.grappim.taigamobile.feature.dashboard.ui

import androidx.navigation.NavController
import popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object DashboardNavDestination

fun NavController.navigateToDashboardAsTopDestination() {
    navigate(route = DashboardNavDestination) {
        popUpToTop(this@navigateToDashboardAsTopDestination)
    }
}
