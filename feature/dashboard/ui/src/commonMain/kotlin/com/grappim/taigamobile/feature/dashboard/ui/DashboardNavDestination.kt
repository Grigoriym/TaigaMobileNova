package com.grappim.taigamobile.feature.dashboard.ui

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object DashboardNavDestination : NavKey

fun NavController.navigateToDashboardAsTopDestination() {
    navigate(route = DashboardNavDestination) {
        popUpToTop(this@navigateToDashboardAsTopDestination)
    }
}
