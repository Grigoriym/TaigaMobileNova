package com.grappim.taigamobile.feature.projects.dto.tags

import kotlinx.serialization.Serializable

@Serializable
data class CreateTagRequestDTO(
    val tag: String,
    // Optional, HEX format: "#ffffff" or "#fff"
    val color: String? = null
)
