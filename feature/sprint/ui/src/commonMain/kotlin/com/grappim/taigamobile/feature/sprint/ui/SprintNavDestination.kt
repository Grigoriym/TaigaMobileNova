package com.grappim.taigamobile.feature.sprint.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class SprintNavDestination(val sprintId: Long) : NavKey

fun Navigator.navigateToSprintScreen(sprintId: Long) {
    navigate(SprintNavDestination(sprintId))
}
