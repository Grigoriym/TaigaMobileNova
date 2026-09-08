package com.grappim.taigamobile.feature.settings.ui.projectdetails

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object ProjectDetailsNavDestination : NavKey

fun Navigator.navigateToProjectDetails() {
    navigate(ProjectDetailsNavDestination)
}
