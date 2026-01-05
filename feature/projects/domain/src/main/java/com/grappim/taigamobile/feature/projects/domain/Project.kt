package com.grappim.taigamobile.feature.projects.domain

import kotlinx.collections.immutable.ImmutableList

data class Project(
    val id: Long,
    val name: String,
    val slug: String,
    val isMember: Boolean,
    val isAdmin: Boolean,
    val isOwner: Boolean,
    val description: String?,
    val avatarUrl: String?,
    val members: ImmutableList<Long>,
    val fansCount: Int,
    val watchersCount: Int,
    val isPrivate: Boolean,
    val myPermissions: ImmutableList<TaigaPermission>,
    val isEpicsActivated: Boolean,
    val isBacklogActivated: Boolean,
    val isKanbanActivated: Boolean,
    val isIssuesActivated: Boolean,
    val isWikiActivated: Boolean
)

data class ProjectSimple(
    val id: Long,
    val name: String,
    val slug: String,
    val myPermissions: ImmutableList<TaigaPermission>,
    val isEpicsActivated: Boolean,
    val isBacklogActivated: Boolean,
    val isKanbanActivated: Boolean,
    val isIssuesActivated: Boolean,
    val isWikiActivated: Boolean
)
