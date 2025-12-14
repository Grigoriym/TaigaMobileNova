package com.grappim.taigamobile.feature.users.data.mappers

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.core.domain.StatsDTO
import com.grappim.taigamobile.feature.users.domain.UserStats
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserStatsMapper @Inject constructor(@DefaultDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toDomain(dto: StatsDTO): UserStats = withContext(dispatcher) {
        UserStats(
            roles = dto.roles.toImmutableList(),
            totalNumClosedUserStories = dto.totalNumClosedUserStories,
            totalNumContacts = dto.totalNumContacts,
            totalNumProjects = dto.totalNumProjects
        )
    }
}
