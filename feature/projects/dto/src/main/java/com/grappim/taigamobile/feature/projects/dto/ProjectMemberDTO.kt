package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectMemberDTO(
    val id: Long,
    val photo: String?,
    @SerialName(value = "full_name_display")
    val fullNameDisplay: String,
    @SerialName(value = "role_name")
    val roleName: String,
    val username: String
)
