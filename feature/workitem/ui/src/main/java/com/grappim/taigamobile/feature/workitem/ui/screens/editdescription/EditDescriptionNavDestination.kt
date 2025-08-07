package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemEditDescriptionNavDestination(val description: String)

fun NavController.navigateToWorkItemEditDescription(description: String) {
    navigate(route = WorkItemEditDescriptionNavDestination(description = description))
}
