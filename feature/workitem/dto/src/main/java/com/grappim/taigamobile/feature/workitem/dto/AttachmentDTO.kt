package com.grappim.taigamobile.feature.workitem.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttachmentDTO(
    val id: Long,
    val name: String,
    @SerialName(value = "size") val sizeInBytes: Long,
    val url: String
)
