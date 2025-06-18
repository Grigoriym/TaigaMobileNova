package io.eugenethedev.taigamobile.profile

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class ProfileNavDestination(
    val userId: Long
)

fun NavController.navigateToProfileScreen(userId: Long) {
    navigate(route = ProfileNavDestination(userId))
}
