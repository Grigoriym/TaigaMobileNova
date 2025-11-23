package com.grappim.taigamobile.feature.users.domain

import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.TeamMemberDTO
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.UserDTO
import kotlinx.collections.immutable.ImmutableList

interface UsersRepository {
    suspend fun getMe(): UserDTO
    suspend fun getMeResult(): Result<UserDTO>
    suspend fun getUserDTO(userId: Long): UserDTO
    suspend fun getUser(userId: Long): User
    suspend fun getUsersList(ids: List<Long>): ImmutableList<User>
    suspend fun isAnyAssignedToMe(list: ImmutableList<User>): Boolean
    suspend fun getUserStats(userId: Long): Stats

    suspend fun getTeamSimpleOld(): List<TeamMemberDTO>

    suspend fun getTeamOld(): Result<List<TeamMemberDTO>>
    suspend fun getTeamByProjectIdOld(projectId: Long): Result<List<TeamMemberDTO>>

    suspend fun getCurrentTeam(generateMemberStats: Boolean = false): ImmutableList<TeamMember>

    suspend fun getCurrentTeamResult(generateMemberStats: Boolean = false): Result<ImmutableList<TeamMember>>
}
