package com.grappim.taigamobile.feature.issues.ui.details

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class IssueDetailsNavDestination(val issueId: Long, val ref: Long)

fun NavController.navigateToIssueDetails(issueId: Long, ref: Long) {
    navigate(route = IssueDetailsNavDestination(issueId = issueId, ref = ref))
}
