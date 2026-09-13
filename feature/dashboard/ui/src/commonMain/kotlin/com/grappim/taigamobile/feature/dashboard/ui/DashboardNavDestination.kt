package com.grappim.taigamobile.feature.dashboard.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object DashboardNavDestination : NavKey

fun Navigator.navigateToDashboardAsTopDestination() {
    resetTo(DashboardNavDestination)
}
