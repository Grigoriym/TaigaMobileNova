package com.grappim.taigamobile.feature.tasks.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class TaskDetailsNavDestination(val taskId: Long, val ref: Long) : NavKey

fun Navigator.navigateToTask(taskId: Long, ref: Long) {
    navigate(TaskDetailsNavDestination(taskId, ref))
}
