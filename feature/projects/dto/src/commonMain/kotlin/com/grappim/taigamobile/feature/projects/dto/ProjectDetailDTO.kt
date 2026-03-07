package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDetailDTO(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String,
    @SerialName("i_am_member") val isMember: Boolean = false,
    @SerialName("i_am_admin") val isAdmin: Boolean = false,
    @SerialName("i_am_owner") val isOwner: Boolean = false,
    @SerialName("logo_small_url") val avatarUrl: String? = null,
    @SerialName("total_fans") val fansCount: Int = 0,
    @SerialName("total_watchers") val watchersCount: Int = 0,
    @SerialName("is_private") val isPrivate: Boolean = false,
    @SerialName("my_permissions") val myPermissions: List<TaigaPermissionDTO> = emptyList(),
    @SerialName("is_epics_activated") val isEpicsActivated: Boolean = false,
    @SerialName("is_backlog_activated") val isBacklogActivated: Boolean = false,
    @SerialName("is_kanban_activated") val isKanbanActivated: Boolean = false,
    @SerialName("is_issues_activated") val isIssuesActivated: Boolean = false,
    @SerialName("is_wiki_activated") val isWikiActivated: Boolean = false,
    @SerialName("default_swimlane") val defaultSwimlane: Long? = null,
    @SerialName("is_contact_activated") val isContactActivated: Boolean = false,
    @SerialName("is_looking_for_people") val isLookingForPeople: Boolean = false,
    @SerialName("looking_for_people_note") val lookingForPeopleNote: String = "",
    @SerialName("total_milestones") val totalMilestones: Int? = null,
    @SerialName("total_story_points") val totalStoryPoints: Double? = null,
    @SerialName("videoconferences") val videoconferences: String? = null,
    @SerialName("videoconferences_extra_data") val videoconferencesExtraData: String? = null
)
