package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.domain.UserStats
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class FakeUsersRepository : UsersRepository {

    var getUsersListResult: ImmutableList<User> = persistentListOf()
    var getUsersListThrows: Throwable? = null
    var isAnyAssignedToMeResult: Boolean = false
    var isAnyAssignedToMeThrows: Throwable? = null
    var getUserResult: User? = null
    var getUserThrows: Throwable? = null

    var getMeResult: User? = null
    var getMeThrows: Throwable? = null
    var getMeCallCount: Int = 0

    var getUserStatsResult: UserStats? = null
    var getUserStatsThrows: Throwable? = null

    var getTeamMembersResult: ImmutableList<TeamMember> = persistentListOf()
    var getTeamMembersThrows: Throwable? = null
    var getTeamMembersCallCount: Int = 0
    var getTeamMembersGenerateMemberStats: Boolean? = null

    override suspend fun getTeamMembers(generateMemberStats: Boolean): ImmutableList<TeamMember> {
        getTeamMembersCallCount++
        getTeamMembersGenerateMemberStats = generateMemberStats
        getTeamMembersThrows?.let { throw it }
        return getTeamMembersResult
    }

    override suspend fun getTeamMembersByProjectId(
        projectId: Long,
        generateMemberStats: Boolean
    ): ImmutableList<TeamMember> = error("not used in this test")

    override suspend fun getMe(): User {
        getMeCallCount++
        getMeThrows?.let { throw it }
        return getMeResult ?: error("getMeResult not set")
    }

    override suspend fun getUser(userId: Long): User {
        getUserThrows?.let { throw it }
        return getUserResult ?: error("getUserResult not set")
    }

    override suspend fun getUsersList(ids: List<Long>): ImmutableList<User> {
        getUsersListThrows?.let { throw it }
        return getUsersListResult
    }

    override suspend fun isAnyAssignedToMe(list: ImmutableList<User>): Boolean {
        isAnyAssignedToMeThrows?.let { throw it }
        return isAnyAssignedToMeResult
    }

    override suspend fun getUserStats(userId: Long): UserStats {
        getUserStatsThrows?.let { throw it }
        return getUserStatsResult ?: error("getUserStatsResult not set")
    }
}
