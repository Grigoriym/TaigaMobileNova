package com.grappim.taigamobile.feature.workitem.ui.screens.sprint

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditSprintNavDestination(val workItemId: Long, val taskIdentifier: TaskIdentifier) : NavKey

fun Navigator.navigateToWorkItemEditSprint(workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        WorkItemEditSprintNavDestination(
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
