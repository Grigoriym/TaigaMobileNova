package com.grappim.taigamobile.feature.workitem.ui.screens.epic

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditEpicNavDestination(val workItemId: Long, val taskIdentifier: TaskIdentifier) : NavKey

fun Navigator.navigateToWorkItemEditEpic(workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        WorkItemEditEpicNavDestination(
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
