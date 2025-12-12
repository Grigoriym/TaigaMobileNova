package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.core.api.UserMapper
import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.TeamMemberDTO
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.UserDTO
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.users.data.mappers.TeamMemberMapper
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.users.domain.UsersRepository
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
    private val teamMemberMapper: TeamMemberMapper
) : UsersRepository {

    override suspend fun getMe(): UserDTO = usersApi.getMyProfile()

    override suspend fun getMeResult(): Result<UserDTO> = resultOf { getMe() }

    @Deprecated("use getUser")
    override suspend fun getUserDTO(userId: Long): UserDTO = usersApi.getUser(userId)

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

    override suspend fun getUserStats(userId: Long): Stats = usersApi.getUserStats(userId)

    override suspend fun getTeamByProjectIdOld(projectId: Long): Result<List<TeamMemberDTO>> = resultOf {
        getTeamOld(projectId)
    }

    override suspend fun getTeamOld(): Result<List<TeamMemberDTO>> = resultOf {
        getTeamOld(taigaStorage.currentProjectIdFlow.first())
    }

    override suspend fun getTeamSimpleOld(): List<TeamMemberDTO> = getTeamOld(taigaStorage.currentProjectIdFlow.first())

    private suspend fun getTeamOld(projectId: Long): List<TeamMemberDTO> = coroutineScope {
        val team = async { projectsApi.getProject(projectId).members }
        val stats = async { retrieveMembersStats() }

        val statsResult = stats.await()
        val teamResult = team.await()

        teamResult.map {
            TeamMemberDTO(
                id = it.id,
                avatarUrl = it.photo,
                name = it.fullNameDisplay,
                role = it.roleName,
                username = it.username,
                totalPower = statsResult[it.id] ?: 0
            )
        }
    }

    override suspend fun getCurrentTeam(generateMemberStats: Boolean): ImmutableList<TeamMember> = coroutineScope {
        val currentProjectId = taigaStorage.currentProjectIdFlow.first()

        val team = projectsApi.getProject(currentProjectId).members
        val stats: Map<Long, Int> = if (generateMemberStats) {
            retrieveMembersStats()
        } else {
            emptyMap()
        }
        teamMemberMapper.toDomain(team, stats)
    }

    override suspend fun getCurrentTeamResult(generateMemberStats: Boolean): Result<ImmutableList<TeamMember>> =
        resultOf {
            getCurrentTeam(generateMemberStats)
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
