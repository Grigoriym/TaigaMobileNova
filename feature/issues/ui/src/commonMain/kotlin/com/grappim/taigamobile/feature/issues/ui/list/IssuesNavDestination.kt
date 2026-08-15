package com.grappim.taigamobile.feature.issues.ui.list

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object IssuesNavDestination : NavKey

fun NavController.navigateToIssues(navOptions: NavOptions) {
    navigate(route = IssuesNavDestination, navOptions = navOptions)
}
