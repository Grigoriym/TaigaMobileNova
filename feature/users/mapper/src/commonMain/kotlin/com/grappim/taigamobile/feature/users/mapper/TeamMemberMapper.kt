package com.grappim.taigamobile.feature.users.mapper

import com.grappim.taigamobile.feature.projects.dto.ProjectMemberDTO
import com.grappim.taigamobile.feature.users.domain.TeamMember
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Factory

@Factory
class TeamMemberMapper {
    fun toDomain(list: List<ProjectMemberDTO>, stats: Map<Long, Int>): ImmutableList<TeamMember> = list.map { dto ->
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
