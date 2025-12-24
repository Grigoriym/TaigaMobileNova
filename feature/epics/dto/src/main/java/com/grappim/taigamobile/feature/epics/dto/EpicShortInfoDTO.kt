package com.grappim.taigamobile.feature.epics.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpicShortInfoDTO(
    val id: Long,
    @SerialName(value = "subject")
    val title: String,
    val ref: Long,
    val color: String
)
