package com.grappim.taigamobile.feature.userstories.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateUserStoryRequest(
    val project: Long,
    val subject: String,
    val description: String,
    val status: Long?,
    val swimlane: Long?
)
