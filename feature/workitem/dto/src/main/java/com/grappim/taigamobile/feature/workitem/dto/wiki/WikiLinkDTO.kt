package com.grappim.taigamobile.feature.workitem.dto.wiki

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WikiLinkDTO(
    val id: Long,
    @SerialName("project")
    val projectId: Long,
    val title: String,
    val href: String,
    val order: Long
)
