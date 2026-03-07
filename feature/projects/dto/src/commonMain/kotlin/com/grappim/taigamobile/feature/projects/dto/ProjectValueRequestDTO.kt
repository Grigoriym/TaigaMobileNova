package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectValueRequestDTO(
    val project: Long? = null,
    val name: String,
    val color: String? = null,
    val order: Int? = null,
    @SerialName("is_closed") val isClosed: Boolean? = null,
    @SerialName("is_archived") val isArchived: Boolean? = null,
    val value: Double? = null,
    @SerialName("days_to_due") val daysToDue: Int? = null
)
