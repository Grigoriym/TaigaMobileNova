package com.grappim.taigamobile.feature.epics.ui.list

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object EpicsNavDestination : NavKey

fun Navigator.navigateToEpics() {
    navigate(EpicsNavDestination)
}
