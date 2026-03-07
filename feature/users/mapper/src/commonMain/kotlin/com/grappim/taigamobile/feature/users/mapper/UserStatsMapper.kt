package com.grappim.taigamobile.feature.users.mapper

import com.grappim.taigamobile.feature.users.domain.UserStats
import com.grappim.taigamobile.feature.users.dto.StatsDTO
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Factory

@Factory
class UserStatsMapper {

    fun toDomain(dto: StatsDTO): UserStats = UserStats(
        roles = dto.roles.toImmutableList(),
        totalNumClosedUserStories = dto.totalNumClosedUserStories,
        totalNumContacts = dto.totalNumContacts,
        totalNumProjects = dto.totalNumProjects
    )
}
