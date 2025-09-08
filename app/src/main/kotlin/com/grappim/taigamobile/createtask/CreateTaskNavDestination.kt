package com.grappim.taigamobile.createtask

import androidx.navigation.NavController
import com.grappim.taigamobile.core.domain.CommonTaskType
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskNavDestination(
    val type: CommonTaskType,
    val parentId: Long?,
    val sprintId: Long?,
    val statusId: Long?,
    val swimlaneId: Long?
)

fun NavController.navigateToCreateIssue() {
    navigateToCreateTask(CommonTaskType.Issue)
}

fun NavController.navigateToCreateTask(
    type: CommonTaskType,
    parentId: Long? = null,
    sprintId: Long? = null,
    statusId: Long? = null,
    swimlaneId: Long? = null
) {
    navigate(route = CreateTaskNavDestination(type, parentId, sprintId, statusId, swimlaneId))
}
