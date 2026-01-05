package com.grappim.taigamobile.feature.epics.ui.details

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class EpicDetailsNavDestination(val epicId: Long, val ref: Long)

fun NavController.navigateToEpicDetails(epicId: Long, ref: Long) {
    navigate(route = EpicDetailsNavDestination(epicId = epicId, ref = ref))
}
