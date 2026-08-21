package com.grappim.taigamobile.feature.sprint.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class SprintNavDestination(val sprintId: Long) : NavKey

fun Navigator.navigateToSprintScreen(sprintId: Long) {
    navigate(SprintNavDestination(sprintId))
}
