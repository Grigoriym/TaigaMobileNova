package com.grappim.taigamobile.feature.issues.ui.details

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class IssueDetailsNavDestination(val taskId: Long, val ref: Int)

fun NavController.navigateToIssueDetails(taskId: Long, ref: Int) {
    navigate(route = IssueDetailsNavDestination(taskId = taskId, ref = ref))
}
