package com.grappim.taigamobile.feature.projectselector.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class ProjectSelectorNavDestination(val isFromLogin: Boolean = false) : NavKey

fun Navigator.navigateToProjectSelector(isFromLogin: Boolean = false) {
    navigate(ProjectSelectorNavDestination(isFromLogin))
}
