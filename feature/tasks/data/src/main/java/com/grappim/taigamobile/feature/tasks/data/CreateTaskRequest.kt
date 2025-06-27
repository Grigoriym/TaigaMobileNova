package com.grappim.taigamobile.feature.tasks.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateTaskRequest(
    val project: Long,
    val subject: String,
    val description: String,
    val milestone: Long?,
    @Json(name = "user_story")
    val userStory: Long?
)
