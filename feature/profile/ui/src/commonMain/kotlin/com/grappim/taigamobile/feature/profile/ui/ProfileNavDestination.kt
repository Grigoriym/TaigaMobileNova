package com.grappim.taigamobile.feature.profile.ui

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ProfileNavDestination(val userId: Long) : NavKey

fun NavController.navigateToProfileScreen(userId: Long) {
    navigate(route = ProfileNavDestination(userId))
}
