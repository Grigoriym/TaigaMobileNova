package com.grappim.taigamobile.feature.profile.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class ProfileNavDestination(val userId: Long) : NavKey

fun Navigator.navigateToProfileScreen(userId: Long) {
    navigate(ProfileNavDestination(userId))
}
