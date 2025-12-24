package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.navigation.NavController
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditDescriptionNavDestination(
    val description: String,
    val workItemId: Long,
    val taskIdentifier: TaskIdentifier
)

fun NavController.navigateToWorkItemEditDescription(
    description: String,
    workItemId: Long,
    taskIdentifier: TaskIdentifier
) {
    navigate(
        route = WorkItemEditDescriptionNavDestination(
            description = description,
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
