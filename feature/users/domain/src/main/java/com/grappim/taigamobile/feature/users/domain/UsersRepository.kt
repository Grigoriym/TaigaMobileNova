package com.grappim.taigamobile.feature.users.domain

import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.TeamMember
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.UserDTO

interface UsersRepository {
    suspend fun getMe(): UserDTO
    suspend fun getMeResult(): Result<UserDTO>
    suspend fun getUserDTO(userId: Long): UserDTO
    suspend fun getUser(userId: Long): User
    suspend fun getUsersList(ids: List<Long>): List<User>
    suspend fun isAnyAssignedToMe(list: List<User>): Boolean
    suspend fun getUserStats(userId: Long): Stats

    suspend fun getTeamSimple(): List<TeamMember>

    suspend fun getTeam(): Result<List<TeamMember>>
    suspend fun getTeamByProjectId(projectId: Long): Result<List<TeamMember>>
}
