package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.navigation.NavController
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditTagsNavDestination(val workItemId: Long, val taskIdentifier: TaskIdentifier)

fun NavController.navigateToWorkItemEditTags(workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        route = WorkItemEditTagsNavDestination(
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
