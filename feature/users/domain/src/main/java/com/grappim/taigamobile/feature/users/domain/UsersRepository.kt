package com.grappim.taigamobile.feature.users.domain

import com.grappim.taigamobile.core.domain.StatsDTO
import com.grappim.taigamobile.core.domain.TeamMemberDTO
import com.grappim.taigamobile.core.domain.UserDTO
import kotlinx.collections.immutable.ImmutableList

interface UsersRepository {

    suspend fun getTeamMembers(generateMemberStats: Boolean = false): ImmutableList<TeamMember>

    suspend fun getTeamMembersByProjectId(
        projectId: Long,
        generateMemberStats: Boolean = false
    ): ImmutableList<TeamMember>

    suspend fun getMe(): UserDTO
    suspend fun getMeResult(): Result<UserDTO>

    @Deprecated("use getUser")
    suspend fun getUserDTO(userId: Long): UserDTO
    suspend fun getUser(userId: Long): User
    suspend fun getUsersList(ids: List<Long>): ImmutableList<User>
    suspend fun isAnyAssignedToMe(list: ImmutableList<User>): Boolean

    @Deprecated("remove it")
    suspend fun getUserStatsOld(userId: Long): StatsDTO

    suspend fun getUserStats(userId: Long): UserStats

    suspend fun getTeamSimpleOld(): List<TeamMemberDTO>

    suspend fun getTeamOld(): Result<List<TeamMemberDTO>>
    suspend fun getTeamByProjectIdOld(projectId: Long): Result<List<TeamMemberDTO>>
    suspend fun getCurrentTeamResult(generateMemberStats: Boolean = false): Result<ImmutableList<TeamMember>>
}
