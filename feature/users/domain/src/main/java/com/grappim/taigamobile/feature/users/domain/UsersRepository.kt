package com.grappim.taigamobile.feature.users.domain

import kotlinx.collections.immutable.ImmutableList

interface UsersRepository {

    suspend fun getTeamMembers(generateMemberStats: Boolean = false): ImmutableList<TeamMember>

    suspend fun getTeamMembersByProjectId(
        projectId: Long,
        generateMemberStats: Boolean = false
    ): ImmutableList<TeamMember>

    suspend fun getMe(): User

    suspend fun getUser(userId: Long): User
    suspend fun getUsersList(ids: List<Long>): ImmutableList<User>
    suspend fun isAnyAssignedToMe(list: ImmutableList<User>): Boolean

    suspend fun getUserStats(userId: Long): UserStats
}
