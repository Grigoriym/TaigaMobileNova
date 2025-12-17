package com.grappim.taigamobile.feature.userstories.ui

import androidx.navigation.NavController
import com.grappim.taigamobile.core.navigation.navigateAndPopCurrent
import kotlinx.serialization.Serializable

@Serializable
data class UserStoryDetailsNavDestination(val userStoryId: Long, val ref: Long)

fun NavController.navigateToUserStory(taskId: Long, ref: Long, popUpToRoute: Any? = null) {
    val route = UserStoryDetailsNavDestination(taskId, ref)
    if (popUpToRoute != null) {
        navigateAndPopCurrent(route = route, popUpToRoute = popUpToRoute)
    } else {
        navigate(route = route)
    }
}
