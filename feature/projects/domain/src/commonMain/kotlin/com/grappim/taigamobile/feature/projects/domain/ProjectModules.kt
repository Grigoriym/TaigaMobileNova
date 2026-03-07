package com.grappim.taigamobile.feature.projects.domain

data class ProjectModules(
    val isEpicsActivated: Boolean,
    val isBacklogActivated: Boolean,
    val isKanbanActivated: Boolean,
    val isIssuesActivated: Boolean,
    val isWikiActivated: Boolean,
    val isContactActivated: Boolean,
    val totalMilestones: Int?,
    val totalStoryPoints: Double?,
    val videoconferencesExtraData: String?
)
