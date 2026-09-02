package com.grappim.taigamobile.createtask

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskNavDestination(
    val type: CommonTaskType,
    val parentId: Long?,
    val sprintId: Long?,
    val statusId: Long?,
    val swimlaneId: Long?
) : NavKey

fun Navigator.navigateToCreateIssue() {
    navigateToCreateTask(CommonTaskType.Issue)
}

fun Navigator.navigateToCreateTask(
    type: CommonTaskType,
    parentId: Long? = null,
    sprintId: Long? = null,
    statusId: Long? = null,
    swimlaneId: Long? = null
) {
    navigate(CreateTaskNavDestination(type, parentId, sprintId, statusId, swimlaneId))
}
