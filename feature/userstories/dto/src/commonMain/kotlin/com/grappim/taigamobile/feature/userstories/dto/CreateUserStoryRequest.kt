package com.grappim.taigamobile.feature.userstories.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserStoryRequest(
    val project: Long,
    val subject: String,
    val description: String,
    val status: Long?,
    val swimlane: Long?
)
