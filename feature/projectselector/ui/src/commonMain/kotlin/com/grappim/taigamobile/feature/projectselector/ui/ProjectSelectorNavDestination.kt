package com.grappim.taigamobile.feature.projectselector.ui

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ProjectSelectorNavDestination(val isFromLogin: Boolean = false) : NavKey

fun NavController.navigateToProjectSelector(
    isFromLogin: Boolean = false,
    navOptions: NavOptionsBuilder.() -> Unit = {}
) = navigate(route = ProjectSelectorNavDestination(isFromLogin)) {
    navOptions()
}
