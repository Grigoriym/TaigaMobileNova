package com.grappim.taigamobile.feature.issues.ui.list

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object IssuesNavDestination : NavKey

fun Navigator.navigateToIssues() {
    navigate(IssuesNavDestination)
}
