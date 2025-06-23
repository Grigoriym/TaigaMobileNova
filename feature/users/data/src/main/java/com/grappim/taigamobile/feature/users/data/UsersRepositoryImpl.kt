package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.core.api.withIO
import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.TeamMember
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import kotlinx.coroutines.async
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi,
    private val projectsApi: ProjectsApi,
    private val session: Session
) : UsersRepository {
    private val currentProjectId get() = session.currentProject

    override suspend fun getMe(): User = usersApi.getMyProfile()

    override suspend fun getUser(userId: Long): User = usersApi.getUser(userId)

    override suspend fun getUserStats(userId: Long): Stats = usersApi.getUserStats(userId)

    override suspend fun getTeam(): List<TeamMember> = withIO {
        val team = async { projectsApi.getProject(currentProjectId).members }
        val stats = async {
            usersApi.getMemberStats(currentProjectId).run {
                // calculating total number of points for each id
                (
                    closedBugs.toList() + closedTasks.toList() + createdBugs.toList() +
                        iocaineTasks.toList() + wikiChanges.toList()
                    )
                    .mapNotNull { p -> p.first.toLongOrNull()?.let { it to p.second } }
                    .groupBy { it.first }
                    .map { (k, v) -> k to v.sumOf { it.second } }
                    .toMap()
            }
        }

        stats.await().let { stat ->
            team.await().map {
                TeamMember(
                    id = it.id,
                    avatarUrl = it.photo,
                    name = it.fullNameDisplay,
                    role = it.roleName,
                    username = it.username,
                    totalPower = stat[it.id] ?: 0
                )
            }
        }
    }
}
