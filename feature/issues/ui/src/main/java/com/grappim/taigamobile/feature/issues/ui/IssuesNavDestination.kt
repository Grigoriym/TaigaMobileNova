package com.grappim.taigamobile.feature.issues.ui

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
data object IssuesNavDestination

fun NavController.navigateToIssues(navOptions: NavOptions) {
    navigate(route = IssuesNavDestination, navOptions = navOptions)
}
