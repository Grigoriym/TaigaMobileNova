package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * It represents the full info on the project
 */
@Serializable
data class ProjectDTO(
    val id: Long,
    val name: String,
    val slug: String,
    @SerialName("i_am_member") val isMember: Boolean = false,
    @SerialName("i_am_admin") val isAdmin: Boolean = false,
    @SerialName("i_am_owner") val isOwner: Boolean = false,
    val description: String,
    @SerialName("logo_small_url") val avatarUrl: String? = null,
    val members: List<Long> = emptyList(),
    @SerialName("total_fans") val fansCount: Int = 0,
    @SerialName("total_watchers") val watchersCount: Int = 0,
    @SerialName("is_watcher") val isWatcher: Boolean = false,
    @SerialName("is_private") val isPrivate: Boolean = false,
    @SerialName("my_permissions") val myPermissions: List<TaigaPermissionDTO> = emptyList(),
    @SerialName("is_epics_activated") val isEpicsActivated: Boolean,
    @SerialName("is_backlog_activated") val isBacklogActivated: Boolean,
    @SerialName("is_kanban_activated") val isKanbanActivated: Boolean,
    @SerialName("is_issues_activated") val isIssuesActivated: Boolean,
    @SerialName("is_wiki_activated") val isWikiActivated: Boolean,
    @SerialName("default_swimlane")
    val defaultSwimlane: Long? = null
)
