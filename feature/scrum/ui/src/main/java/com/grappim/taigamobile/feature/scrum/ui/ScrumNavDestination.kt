package com.grappim.taigamobile.feature.scrum.ui

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
data object ScrumNavDestination

fun NavController.navigateToScrum(navOptions: NavOptions) {
    navigate(route = ScrumNavDestination, navOptions = navOptions)
}
