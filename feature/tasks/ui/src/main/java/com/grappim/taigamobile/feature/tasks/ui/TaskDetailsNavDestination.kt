package com.grappim.taigamobile.feature.tasks.ui

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class TaskDetailsNavDestination(val taskId: Long, val ref: Long)

fun NavController.navigateToTask(taskId: Long, ref: Long) {
    navigate(route = TaskDetailsNavDestination(taskId, ref))
}
