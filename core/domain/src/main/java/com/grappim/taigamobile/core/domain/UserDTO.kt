package com.grappim.taigamobile.core.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Users related entities
 */
@JsonClass(generateAdapter = true)
data class UserDTO(
    @Json(name = "id") val id: Long?,
    @Json(name = "full_name_display") val fullName: String?,
    val photo: String?,
    @Json(name = "big_photo") val bigPhoto: String?,
    val username: String,
    // sometimes name appears here
    val name: String? = null,
    val pk: Long? = null
) {
    val displayName get() = fullName ?: name!!
    val avatarUrl get() = bigPhoto ?: photo
    val actualId get() = id ?: pk!!
}

data class TeamMemberDTO(
    val id: Long,
    val avatarUrl: String?,
    val name: String,
    val role: String,
    val username: String,
    val totalPower: Int
) {
    fun toUser() = UserDTO(
        id = id,
        fullName = name,
        photo = avatarUrl,
        bigPhoto = null,
        username = username
    )
}

@JsonClass(generateAdapter = true)
data class Stats(
    val roles: List<String> = emptyList(),
    @Json(name = "total_num_closed_userstories")
    val totalNumClosedUserStories: Int,
    @Json(name = "total_num_contacts")
    val totalNumContacts: Int,
    @Json(name = "total_num_projects")
    val totalNumProjects: Int
)
