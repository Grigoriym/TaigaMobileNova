package com.grappim.taigamobile.feature.settings.ui.projectdetails

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
object ProjectDetailsNavDestination : NavKey

fun Navigator.navigateToProjectDetails() {
    navigate(ProjectDetailsNavDestination)
}
