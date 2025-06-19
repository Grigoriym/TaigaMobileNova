package com.grappim.taigamobile.data.repositories

import com.grappim.taigamobile.core.api.withIO
import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.TeamMember
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.data.api.TaigaApi
import com.grappim.taigamobile.domain.repositories.IUsersRepository
import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import kotlinx.coroutines.async
import javax.inject.Inject

class UsersRepository @Inject constructor(
    private val taigaApi: TaigaApi,
    private val projectsApi: ProjectsApi,
    private val session: Session
) : IUsersRepository {
    private val currentProjectId get() = session.currentProjectId.value

    override suspend fun getMe() = taigaApi.getMyProfile()

    override suspend fun getUser(userId: Long) = taigaApi.getUser(userId)

    override suspend fun getUserStats(userId: Long): Stats = taigaApi.getUserStats(userId)

    override suspend fun getTeam() = withIO {
        val team = async { projectsApi.getProject(currentProjectId).members }
        val stats = async {
            taigaApi.getMemberStats(currentProjectId).run {
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
