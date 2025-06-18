package com.grappim.taigamobile.sprint

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class SprintNavDestination(
    val sprintId: Long
)

fun NavController.navigateToSprintScreen(sprintId: Long) {
    navigate(route = SprintNavDestination(sprintId))
}
