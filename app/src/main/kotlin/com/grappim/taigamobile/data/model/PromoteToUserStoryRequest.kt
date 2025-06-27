package com.grappim.taigamobile.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PromoteToUserStoryRequest(
    @Json(name = "project_id")
    val projectId: Long
)
