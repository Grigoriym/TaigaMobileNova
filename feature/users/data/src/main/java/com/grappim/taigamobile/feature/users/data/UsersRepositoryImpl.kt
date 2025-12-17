package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.domain.UserStats
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.users.mapper.TeamMemberMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.users.mapper.UserStatsMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi,
    private val projectsApi: ProjectsApi,
    private val taigaStorage: TaigaStorage,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val userMapper: UserMapper,
    private val session: Session,
    private val teamMemberMapper: TeamMemberMapper,
    private val userStatsMapper: UserStatsMapper
) : UsersRepository {

    override suspend fun getTeamMembers(generateMemberStats: Boolean): ImmutableList<TeamMember> =
        getTeamMembersByProjectId(
            projectId = taigaStorage.currentProjectIdFlow.first(),
            generateMemberStats = generateMemberStats
        )

    override suspend fun getTeamMembersByProjectId(
        projectId: Long,
        generateMemberStats: Boolean
    ): ImmutableList<TeamMember> = coroutineScope {
        val team = projectsApi.getProject(projectId).members
        val stats: Map<Long, Int> = if (generateMemberStats) {
            retrieveMembersStats()
        } else {
            emptyMap()
        }
        teamMemberMapper.toDomain(team, stats)
    }

    override suspend fun getMe(): User {
        val result = usersApi.getMyProfile()
        return userMapper.toUser(result)
    }

    override suspend fun getUser(userId: Long): User {
        val dto = usersApi.getUser(userId)
        return userMapper.toUser(dto)
    }

    override suspend fun getUsersList(ids: List<Long>): ImmutableList<User> = coroutineScope {
        ids.map { id ->
            async { getUser(id) }
        }.awaitAll().toImmutableList()
    }

    override suspend fun isAnyAssignedToMe(list: ImmutableList<User>): Boolean = withContext(dispatcher) {
        session.userId in list.map { it.actualId }
    }

    override suspend fun getUserStats(userId: Long): UserStats {
        val response = usersApi.getUserStats(userId)
        return userStatsMapper.toDomain(response)
    }

    /**
     * This one calculates the "Total power" of team members
     * Also present on taiga-front
     */
    private suspend fun retrieveMembersStats(): Map<Long, Int> {
        val response = usersApi.getMemberStats(taigaStorage.currentProjectIdFlow.first())

        return withContext(dispatcher) {
            listOf(
                response.closedBugs.toList(),
                response.closedTasks.toList(),
                response.createdBugs.toList(),
                response.iocaineTasks.toList(),
                response.wikiChanges.toList()
            ).flatten()
                .mapNotNull { (key, value) ->
                    key.toLongOrNull()?.let { it to value }
                }
                .groupingBy { it.first }
                .fold(0) { acc, pair -> acc + pair.second }
        }
    }
}
