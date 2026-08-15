package com.grappim.taigamobile.feature.epics.ui.list

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object EpicsNavDestination : NavKey

fun NavController.navigateToEpics(navOptions: NavOptions) {
    navigate(route = EpicsNavDestination, navOptions = navOptions)
}
