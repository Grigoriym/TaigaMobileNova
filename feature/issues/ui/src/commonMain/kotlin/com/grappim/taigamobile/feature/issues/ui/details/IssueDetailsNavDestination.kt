package com.grappim.taigamobile.feature.issues.ui.details

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class IssueDetailsNavDestination(val issueId: Long, val ref: Long) : NavKey

fun Navigator.navigateToIssueDetails(issueId: Long, ref: Long) {
    navigate(IssueDetailsNavDestination(issueId = issueId, ref = ref))
}
