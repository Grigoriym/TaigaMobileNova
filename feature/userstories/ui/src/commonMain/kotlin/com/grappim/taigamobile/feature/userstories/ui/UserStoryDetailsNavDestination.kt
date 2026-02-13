package com.grappim.taigamobile.feature.userstories.ui

import androidx.navigation.NavController
import kotlinx.serialization.Serializable
import navigateAndPopCurrent

@Serializable
data class UserStoryDetailsNavDestination(val userStoryId: Long, val ref: Long)

fun NavController.navigateToUserStory(userStoryId: Long, ref: Long, popUpToRoute: Any? = null) {
    val route = UserStoryDetailsNavDestination(userStoryId = userStoryId, ref = ref)
    if (popUpToRoute != null) {
        navigateAndPopCurrent(route = route, popUpToRoute = popUpToRoute)
    } else {
        navigate(route = route)
    }
}
