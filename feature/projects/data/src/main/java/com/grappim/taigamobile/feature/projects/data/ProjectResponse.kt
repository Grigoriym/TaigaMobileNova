package com.grappim.taigamobile.feature.projects.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProjectResponse(
    val id: Long,
    val name: String,
    val members: List<Member>
) {
    @JsonClass(generateAdapter = true)
    data class Member(
        val id: Long,
        val photo: String?,
        val full_name_display: String,
        val role_name: String,
        val username: String
    )
}
