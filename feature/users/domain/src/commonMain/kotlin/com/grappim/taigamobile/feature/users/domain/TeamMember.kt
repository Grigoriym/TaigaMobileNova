package com.grappim.taigamobile.feature.users.domain

data class TeamMember(
    val id: Long,
    val avatarUrl: String?,
    val name: String,
    val role: String,
    val username: String,
    val totalPower: Int? = null
)
