package com.grappim.taigamobile.feature.projects.domain

data class Project(
    val id: Long,
    val name: String,
    val slug: String,
    val isMember: Boolean,
    val isAdmin: Boolean,
    val isOwner: Boolean,
    val description: String?,
    val avatarUrl: String?,
    val members: List<Long>,
    val fansCount: Int,
    val watchersCount: Int,
    val isPrivate: Boolean
)
