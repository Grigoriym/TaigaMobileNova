package com.grappim.taigamobile.feature.issues.ui.details

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

const val UPDATE_DATA_ON_BACK = "UpdateDataOnBack"

@Serializable
data class IssueDetailsNavDestination(val taskId: Long, val ref: Int)

fun NavController.navigateToIssueDetails(taskId: Long, ref: Int) {
    navigate(route = IssueDetailsNavDestination(taskId = taskId, ref = ref))
}
