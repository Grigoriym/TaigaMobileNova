package com.grappim.taigamobile.feature.workitem.ui.delegates.mentions

import com.grappim.taigamobile.feature.users.domain.TeamMember
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemMentionsDelegate {
    val mentionsState: StateFlow<WorkItemMentionsState>

    suspend fun loadMembers()
}

data class WorkItemMentionsState(val members: PersistentList<TeamMember> = persistentListOf())
