package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDTO(
    val id: Long,
    val name: String,
    val slug: String,
    @SerialName(value = "i_am_member") val isMember: Boolean = false,
    @SerialName(value = "i_am_admin") val isAdmin: Boolean = false,
    @SerialName(value = "i_am_owner") val isOwner: Boolean = false,
    val description: String? = null,
    @SerialName(value = "logo_small_url") val avatarUrl: String? = null,
    val members: List<Long> = emptyList(),
    @SerialName(value = "total_fans") val fansCount: Int = 0,
    @SerialName(value = "total_watchers") val watchersCount: Int = 0,
    @SerialName(value = "is_private") val isPrivate: Boolean = false
)
