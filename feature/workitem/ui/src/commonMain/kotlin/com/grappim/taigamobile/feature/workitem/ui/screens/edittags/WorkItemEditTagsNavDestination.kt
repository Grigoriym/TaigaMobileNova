package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditTagsNavDestination(val workItemId: Long, val taskIdentifier: TaskIdentifier) : NavKey

fun NavController.navigateToWorkItemEditTags(workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        route = WorkItemEditTagsNavDestination(
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
