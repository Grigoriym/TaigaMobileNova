package com.grappim.taigamobile.feature.wiki.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateWikiPageRequestDTO(
    @SerialName("project")
    val projectId: Long,
    val slug: String,
    val content: String = ""
)
