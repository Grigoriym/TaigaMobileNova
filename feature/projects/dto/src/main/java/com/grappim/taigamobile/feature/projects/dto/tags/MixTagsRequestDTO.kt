package com.grappim.taigamobile.feature.projects.dto.tags

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MixTagsRequestDTO(
    @SerialName("from_tags")
    val fromTags: List<String>,

    @SerialName("to_tag")
    val toTag: String
)
