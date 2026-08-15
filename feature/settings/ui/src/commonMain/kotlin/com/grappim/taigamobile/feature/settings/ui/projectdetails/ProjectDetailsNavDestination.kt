package com.grappim.taigamobile.feature.settings.ui.projectdetails

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object ProjectDetailsNavDestination : NavKey

fun NavController.navigateToProjectDetails() {
    navigate(ProjectDetailsNavDestination)
}
