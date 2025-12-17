package com.grappim.taigamobile.feature.workitem.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PromoteToUserStoryRequestDTO(
    @Json(name = "project_id")
    val projectId: Long
)