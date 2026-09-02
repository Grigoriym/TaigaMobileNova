package com.grappim.taigamobile.feature.scrum.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ScrumBacklogDestination : NavKey

@Serializable
data object ScrumOpenSprintsDestination : NavKey

@Serializable
data object ScrumClosedSprintsDestination : NavKey
