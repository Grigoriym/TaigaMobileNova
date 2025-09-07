package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object WorkItemEditTagsNavDestination

fun NavController.navigateToWorkItemEditTags() {
    navigate(route = WorkItemEditTagsNavDestination)
}
