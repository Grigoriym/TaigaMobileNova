package com.grappim.taigamobile.feature.epics.ui.list

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object EpicsNavDestination : NavKey

fun Navigator.navigateToEpics() {
    navigate(EpicsNavDestination)
}
