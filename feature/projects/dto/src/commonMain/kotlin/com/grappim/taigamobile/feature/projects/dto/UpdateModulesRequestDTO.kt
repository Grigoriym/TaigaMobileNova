package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateModulesRequestDTO(
    @SerialName("is_epics_activated") val isEpicsActivated: Boolean,
    @SerialName("is_backlog_activated") val isBacklogActivated: Boolean,
    @SerialName("is_kanban_activated") val isKanbanActivated: Boolean,
    @SerialName("is_issues_activated") val isIssuesActivated: Boolean,
    @SerialName("is_wiki_activated") val isWikiActivated: Boolean,
    @SerialName("total_milestones") val totalMilestones: Int?,
    @SerialName("total_story_points") val totalStoryPoints: Double?
)
