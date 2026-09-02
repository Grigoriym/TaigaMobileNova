package com.grappim.taigamobile.feature.epics.ui.details

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class EpicDetailsNavDestination(val epicId: Long, val ref: Long) : NavKey

fun Navigator.navigateToEpicDetails(epicId: Long, ref: Long) {
    navigate(EpicDetailsNavDestination(epicId = epicId, ref = ref))
}
