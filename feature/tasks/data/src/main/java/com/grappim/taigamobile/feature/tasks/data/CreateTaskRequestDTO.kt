package com.grappim.taigamobile.feature.tasks.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequestDTO(
    val project: Long,
    val subject: String,
    val description: String,
    val milestone: Long?,
    @SerialName(value = "user_story")
    val userStory: Long?
)
