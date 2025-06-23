package com.grappim.taigamobile.feature.dashboard.ui

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.grappim.taigamobile.core.navigation.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object DashboardNavDestination

fun NavController.navigateToDashboardAsTopDestination() {
    navigate(route = DashboardNavDestination) {
        popUpToTop(this@navigateToDashboardAsTopDestination)
    }
}

fun NavController.navigateToDashboard(navOptions: NavOptions) {
    navigate(route = DashboardNavDestination, navOptions = navOptions)
}
