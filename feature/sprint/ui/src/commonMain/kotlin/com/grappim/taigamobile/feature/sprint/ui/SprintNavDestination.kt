package com.grappim.taigamobile.feature.sprint.ui

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class SprintNavDestination(val sprintId: Long) : NavKey

fun NavController.navigateToSprintScreen(sprintId: Long) {
    navigate(route = SprintNavDestination(sprintId))
}
