package com.grappim.taigamobile.feature.settings.ui.projectdetails

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
object ProjectDetailsNavDestination

fun NavController.navigateToProjectDetails() {
    navigate(ProjectDetailsNavDestination)
}
