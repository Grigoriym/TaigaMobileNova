package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * It represents the short info on the project
 */
@Serializable
data class ProjectExtraInfoDTO(
    val id: Long,
    val name: String,
    val slug: String,
    @SerialName("logo_small_url") val logoSmallUrl: String?
)
