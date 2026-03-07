package com.grappim.taigamobile.feature.workitem.ui.screens

import kotlinx.collections.immutable.ImmutableList

sealed interface TeamMemberUpdate {
    data class Assignee(val id: Long?) : TeamMemberUpdate
    data class Assignees(val ids: ImmutableList<Long>) : TeamMemberUpdate
    data class Watchers(val ids: ImmutableList<Long>) : TeamMemberUpdate
    data object Clear : TeamMemberUpdate
}
