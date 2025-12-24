package com.grappim.taigamobile.feature.epics.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkToEpicRequestDTO(
    val epic: String,
    @SerialName(value = "user_story")
    val userStory: Long
)
