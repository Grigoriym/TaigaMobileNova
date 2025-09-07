package com.grappim.taigamobile.feature.workitem.ui.models

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.users.domain.TeamMember
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TeamMemberUIMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun toUI(list: ImmutableList<TeamMember>): PersistentList<TeamMemberUI> =
        withContext(ioDispatcher) {
            list.map { teamMember ->
                TeamMemberUI(
                    id = teamMember.id,
                    name = teamMember.name,
                    avatarUrl = teamMember.avatarUrl
                )
            }.toPersistentList()
        }
}
