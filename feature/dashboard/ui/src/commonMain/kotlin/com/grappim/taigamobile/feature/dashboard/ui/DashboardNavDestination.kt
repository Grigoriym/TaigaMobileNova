package com.grappim.taigamobile.feature.dashboard.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object DashboardNavDestination : NavKey

fun Navigator.navigateToDashboardAsTopDestination() {
    resetTo(DashboardNavDestination)
}
