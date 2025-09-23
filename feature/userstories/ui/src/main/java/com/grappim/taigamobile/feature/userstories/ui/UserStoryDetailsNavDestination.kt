package com.grappim.taigamobile.feature.userstories.ui

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class UserStoryDetailsNavDestination(val userStoryId: Long, val ref: Int)

fun NavController.navigateToUserStory(taskId: Long, ref: Int) {
    navigate(route = UserStoryDetailsNavDestination(taskId, ref))
}
