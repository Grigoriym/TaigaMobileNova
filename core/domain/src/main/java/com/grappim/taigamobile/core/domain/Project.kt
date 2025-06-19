package com.grappim.taigamobile.core.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Project(
    val id: Long,
    val name: String,
    val slug: String,
    @Json(name = "i_am_member") val isMember: Boolean = false,
    @Json(name = "i_am_admin") val isAdmin: Boolean = false,
    @Json(name = "i_am_owner") val isOwner: Boolean = false,
    val description: String? = null,
    @Json(name = "logo_small_url") val avatarUrl: String? = null,
    val members: List<Long> = emptyList(),
    @Json(name = "total_fans") val fansCount: Int = 0,
    @Json(name = "total_watchers") val watchersCount: Int = 0,
    @Json(name = "is_private") val isPrivate: Boolean = false
)
