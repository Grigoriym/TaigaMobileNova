package io.eugenethedev.taigamobile.epics

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
data object EpicsNavDestination

fun NavController.navigateToEpics(navOptions: NavOptions) {
    navigate(route = EpicsNavDestination, navOptions = navOptions)
}
