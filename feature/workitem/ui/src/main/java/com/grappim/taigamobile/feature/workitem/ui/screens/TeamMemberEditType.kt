package com.grappim.taigamobile.feature.workitem.ui.screens

sealed interface TeamMemberEditType {
    data object Assignee : TeamMemberEditType
    data object Assignees : TeamMemberEditType
    data object Watchers : TeamMemberEditType
}
