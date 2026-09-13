package com.grappim.taigamobile.feature.profile.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class ProfileNavDestination(val userId: Long) : NavKey

fun Navigator.navigateToProfileScreen(userId: Long) {
    navigate(ProfileNavDestination(userId))
}
