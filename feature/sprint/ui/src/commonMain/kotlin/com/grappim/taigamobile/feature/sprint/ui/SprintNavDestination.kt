package com.grappim.taigamobile.feature.sprint.ui

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class SprintNavDestination(val sprintId: Long)

fun NavController.navigateToSprintScreen(sprintId: Long) {
    navigate(route = SprintNavDestination(sprintId))
}
