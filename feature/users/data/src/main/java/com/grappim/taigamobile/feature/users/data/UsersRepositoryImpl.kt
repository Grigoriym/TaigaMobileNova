package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.TeamMember
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi,
    private val projectsApi: ProjectsApi,
    private val taigaStorage: TaigaStorage,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UsersRepository {

    override suspend fun getMe(): User = usersApi.getMyProfile()

    override suspend fun getMeResult(): Result<User> = resultOf { getMe() }

    override suspend fun getUser(userId: Long): User = usersApi.getUser(userId)

    override suspend fun getUserStats(userId: Long): Stats = usersApi.getUserStats(userId)

    override suspend fun getTeamByProjectId(projectId: Long): Result<List<TeamMember>> = resultOf {
        getTeam(projectId)
    }

    override suspend fun getTeam(): Result<List<TeamMember>> = resultOf {
        getTeam(taigaStorage.currentProjectIdFlow.first())
    }

    override suspend fun getTeamSimple(): List<TeamMember> =
        getTeam(taigaStorage.currentProjectIdFlow.first())

    private suspend fun getTeam(projectId: Long) = coroutineScope {
        val team = async { projectsApi.getProject(projectId).members }
        val stats = async { retrieveMembersStats() }

        val statsResult = stats.await()
        val teamResult = team.await()

        teamResult.map {
            TeamMember(
                id = it.id,
                avatarUrl = it.photo,
                name = it.fullNameDisplay,
                role = it.roleName,
                username = it.username,
                totalPower = statsResult[it.id] ?: 0
            )
        }
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
