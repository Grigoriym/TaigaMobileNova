package com.grappim.taigamobile.feature.users.data.mappers

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.projects.data.ProjectMemberDTO
import com.grappim.taigamobile.feature.users.domain.TeamMember
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TeamMemberMapper @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) {
    suspend fun toDomain(list: List<ProjectMemberDTO>, stats: Map<Long, Int>): ImmutableList<TeamMember> =
        withContext(dispatcher) {
            list.map { dto ->
                TeamMember(
                    id = dto.id,
                    avatarUrl = dto.photo,
                    name = dto.fullNameDisplay,
                    role = dto.roleName,
                    username = dto.username,
                    totalPower = stats[dto.id]
                )
            }.toImmutableList()
        }
}
