package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectValueItemDTO(
    val id: Long,
    val name: String,
    val color: String = "#999999",
    val order: Int = 10,
    val project: Long,
    @SerialName("is_closed") val isClosed: Boolean = false,
    @SerialName("is_archived") val isArchived: Boolean = false,
    val value: Double? = null,
    @SerialName("days_to_due") val daysToDue: Int? = null,
    @SerialName("by_default") val byDefault: Boolean = false
)
