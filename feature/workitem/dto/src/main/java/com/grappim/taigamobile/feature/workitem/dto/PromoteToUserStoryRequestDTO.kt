package com.grappim.taigamobile.feature.workitem.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromoteToUserStoryRequestDTO(
    @SerialName(value = "project_id")
    val projectId: Long
)
