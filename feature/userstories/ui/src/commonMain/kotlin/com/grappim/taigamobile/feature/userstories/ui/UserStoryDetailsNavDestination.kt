package com.grappim.taigamobile.feature.userstories.ui

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class UserStoryDetailsNavDestination(val userStoryId: Long, val ref: Long) : NavKey

fun Navigator.navigateToUserStory(userStoryId: Long, ref: Long, replaceCurrent: Boolean = false) {
    val route = UserStoryDetailsNavDestination(userStoryId = userStoryId, ref = ref)
    if (replaceCurrent) {
        replaceCurrent(route)
    } else {
        navigate(route)
    }
}
