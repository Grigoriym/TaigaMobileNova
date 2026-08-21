package com.grappim.taigamobile.feature.tasks.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class TaskDetailsNavDestination(val taskId: Long, val ref: Long) : NavKey

fun Navigator.navigateToTask(taskId: Long, ref: Long) {
    navigate(TaskDetailsNavDestination(taskId, ref))
}
