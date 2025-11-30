package com.grappim.taigamobile.feature.epics.ui.details

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class EpicDetailsNavDestination(val epicId: Long, val ref: Int)

fun NavController.navigateToEpicDetails(epicId: Long, ref: Int) {
    navigate(route = EpicDetailsNavDestination(epicId = epicId, ref = ref))
}
