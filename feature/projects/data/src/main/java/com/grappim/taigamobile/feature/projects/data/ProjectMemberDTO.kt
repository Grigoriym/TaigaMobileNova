package com.grappim.taigamobile.feature.projects.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProjectMemberDTO(
    val id: Long,
    val photo: String?,
    @Json(name = "full_name_display")
    val fullNameDisplay: String,
    @Json(name = "role_name")
    val roleName: String,
    val username: String
)
