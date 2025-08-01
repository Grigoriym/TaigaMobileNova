package com.grappim.taigamobile.feature.workitem.ui.screens.editassignees

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

const val WORK_ITEM_WAS_ASSIGNEE_CHANGED = "WORK_ITEM_WAS_ASSIGNEE_CHANGED"

@Serializable
data object WorkItemEditAssigneeNavDestination

fun NavController.navigateToWorkItemEditAssignee() {
    navigate(route = WorkItemEditAssigneeNavDestination)
}
