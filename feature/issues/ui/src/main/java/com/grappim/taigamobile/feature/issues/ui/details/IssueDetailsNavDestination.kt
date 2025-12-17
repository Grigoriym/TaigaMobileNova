package com.grappim.taigamobile.feature.issues.ui.details

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class IssueDetailsNavDestination(val taskId: Long, val ref: Long)

fun NavController.navigateToIssueDetails(taskId: Long, ref: Long) {
    navigate(route = IssueDetailsNavDestination(taskId = taskId, ref = ref))
}
