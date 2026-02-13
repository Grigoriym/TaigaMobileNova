package com.grappim.taigamobile.feature.workitem.ui.screens.epic

import androidx.navigation.NavController
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditEpicNavDestination(val workItemId: Long, val taskIdentifier: TaskIdentifier)

fun NavController.navigateToWorkItemEditEpic(workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        route = WorkItemEditEpicNavDestination(
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
