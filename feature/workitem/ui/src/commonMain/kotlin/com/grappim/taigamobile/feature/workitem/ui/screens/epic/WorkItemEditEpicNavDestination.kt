package com.grappim.taigamobile.feature.workitem.ui.screens.epic

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditEpicNavDestination(val workItemId: Long, val taskIdentifier: TaskIdentifier) : NavKey

fun NavController.navigateToWorkItemEditEpic(workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        route = WorkItemEditEpicNavDestination(
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
