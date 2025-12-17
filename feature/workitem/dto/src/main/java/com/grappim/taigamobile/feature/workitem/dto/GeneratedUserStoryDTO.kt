package com.grappim.taigamobile.feature.workitem.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeneratedUserStoryDTO(
    val id: Long,
    val ref: Long?,
    val subject: String
)
