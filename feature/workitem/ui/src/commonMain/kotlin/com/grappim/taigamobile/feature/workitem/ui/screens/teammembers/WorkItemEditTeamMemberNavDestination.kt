package com.grappim.taigamobile.feature.workitem.ui.screens.teammembers

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import com.grappim.taigamobile.core.domain.TaskIdentifier
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditTeamMemberNavDestination(val workItemId: Long, val taskIdentifier: TaskIdentifier) : NavKey

fun Navigator.navigateToWorkItemEditTeamMember(workItemId: Long, taskIdentifier: TaskIdentifier) {
    navigate(
        WorkItemEditTeamMemberNavDestination(
            workItemId = workItemId,
            taskIdentifier = taskIdentifier
        )
    )
}
