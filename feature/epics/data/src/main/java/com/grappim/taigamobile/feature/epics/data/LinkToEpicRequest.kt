package com.grappim.taigamobile.feature.epics.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkToEpicRequest(
    val epic: String,
    @Json(name = "user_story")
    val userStory: Long
)
