package com.grappim.taigamobile.feature.workitem.ui.screens.teammembers

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object WorkItemEditTeamMemberNavDestination

fun NavController.navigateToWorkItemEditTeamMember() {
    navigate(route = WorkItemEditTeamMemberNavDestination)
}
