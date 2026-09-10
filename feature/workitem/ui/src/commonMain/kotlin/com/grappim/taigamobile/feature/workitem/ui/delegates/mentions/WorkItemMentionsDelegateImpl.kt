package com.grappim.taigamobile.feature.workitem.ui.delegates.mentions

import com.grappim.kit.logger.LogPriority
import com.grappim.kit.logger.logcat
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemMentionsDelegateImpl(private val usersRepository: UsersRepository) : WorkItemMentionsDelegate {

    private val _mentionsState = MutableStateFlow(WorkItemMentionsState())
    override val mentionsState: StateFlow<WorkItemMentionsState> = _mentionsState.asStateFlow()

    override suspend fun loadMembers() {
        resultOf { usersRepository.getTeamMembers() }
            .onSuccess { members ->
                _mentionsState.update { it.copy(members = members.toPersistentList()) }
            }
            .onFailure { error ->
                logcat(LogPriority.ERROR, throwable = error) { "Error loading team members for mentions" }
            }
    }
}
