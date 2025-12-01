package com.grappim.taigamobile.feature.workitem.ui.screens.sprint

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object WorkItemEditSprintNavDestination

fun NavController.navigateToWorkItemEditSprint() {
    navigate(route = WorkItemEditSprintNavDestination)
}
