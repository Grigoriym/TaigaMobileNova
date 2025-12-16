package com.grappim.taigamobile.feature.epics.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkToEpicRequestDTO(
    val epic: String,
    @Json(name = "user_story")
    val userStory: Long
)
