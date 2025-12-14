package com.grappim.taigamobile.feature.users.domain

data class User(
    val id: Long?,
    val fullName: String?,
    val photo: String?,
    val bigPhoto: String?,
    val username: String,
    /**
     * sometimes name appears here
     */
    val name: String? = null,
    val pk: Long? = null
) {
    val avatarUrl get() = bigPhoto ?: photo
    val displayName get() = fullName ?: name!!
    val actualId get() = id ?: pk!!
}
