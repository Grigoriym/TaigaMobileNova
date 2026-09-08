package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditTagsNavDestination(val workItemId: Long, val taskIdentifier: TaskIdentifier) : NavKey

fun Navigator.navigateToWorkItemEditTags(workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        WorkItemEditTagsNavDestination(
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
