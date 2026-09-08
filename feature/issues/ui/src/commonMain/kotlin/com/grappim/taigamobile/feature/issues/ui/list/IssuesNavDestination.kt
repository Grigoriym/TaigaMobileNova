package com.grappim.taigamobile.feature.issues.ui.list

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object IssuesNavDestination : NavKey

fun Navigator.navigateToIssues() {
    navigate(IssuesNavDestination)
}
