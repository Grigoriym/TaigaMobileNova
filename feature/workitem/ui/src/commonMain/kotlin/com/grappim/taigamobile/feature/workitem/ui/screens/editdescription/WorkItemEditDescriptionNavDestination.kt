package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditDescriptionNavDestination(
    val description: String,
    val workItemId: Long,
    val taskIdentifier: TaskIdentifier
) : NavKey

fun Navigator.navigateToWorkItemEditDescription(description: String, workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        WorkItemEditDescriptionNavDestination(
            description = description,
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
