package com.grappim.taigamobile.feature.users.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Users related entities
 */
@Serializable
data class UserDTO(
    @SerialName(value = "id") val id: Long?,
    @SerialName(value = "full_name_display") val fullName: String?,
    val photo: String?,
    @SerialName(value = "big_photo") val bigPhoto: String?,
    val username: String,
    // sometimes name appears here
    val name: String? = null,
    val pk: Long? = null
) {
    val displayName get() = fullName ?: name!!
    val avatarUrl get() = bigPhoto ?: photo
    val actualId get() = id ?: pk!!
}
