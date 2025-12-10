package com.grappim.taigamobile.feature.workitem.ui.screens.epic

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object WorkItemEditEpicNavDestination

fun NavController.navigateToWorkItemEditEpic() {
    navigate(route = WorkItemEditEpicNavDestination)
}
