package com.grappim.taigamobile.feature.workitem.ui.screens.sprint

import androidx.navigation.NavController
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditSprintNavDestination(val workItemId: Long, val taskIdentifier: TaskIdentifier)

fun NavController.navigateToWorkItemEditSprint(workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        route = WorkItemEditSprintNavDestination(
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
