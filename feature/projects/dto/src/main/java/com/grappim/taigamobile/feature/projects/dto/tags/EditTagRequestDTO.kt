package com.grappim.taigamobile.feature.projects.dto.tags

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditTagRequestDTO(
    @SerialName("from_tag")
    val fromTag: String,

    @SerialName("to_tag")
    val toTag: String? = null,

    /**
     * HEX format: #RRGGBB or #RGB
     */
    val color: String? = null
)
