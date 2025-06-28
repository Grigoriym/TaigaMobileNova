package com.grappim.taigamobile.feature.users.domain

import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.TeamMember
import com.grappim.taigamobile.core.domain.User

interface UsersRepository {
    suspend fun getMe(): User
    suspend fun getMeResult(): Result<User>
    suspend fun getUser(userId: Long): User
    suspend fun getUserStats(userId: Long): Stats

    suspend fun getTeamSimple(): List<TeamMember>

    suspend fun getTeam(): Result<List<TeamMember>>
    suspend fun getTeamByProjectId(projectId: Long): Result<List<TeamMember>>
}
