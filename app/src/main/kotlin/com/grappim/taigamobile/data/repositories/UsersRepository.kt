package com.grappim.taigamobile.data.repositories

import com.grappim.taigamobile.data.api.TaigaApi
import com.grappim.taigamobile.domain.entities.Stats
import com.grappim.taigamobile.domain.entities.TeamMember
import com.grappim.taigamobile.domain.repositories.IUsersRepository
import com.grappim.taigamobile.projectselector.ProjectsApi
import com.grappim.taigamobile.state.Session
import kotlinx.coroutines.async
import javax.inject.Inject

class UsersRepository @Inject constructor(
    private val taigaApi: TaigaApi,
    private val projectsApi: ProjectsApi,
    private val session: Session
) : IUsersRepository {
    private val currentProjectId get() = session.currentProjectId.value

    override suspend fun getMe() = withIO { taigaApi.getMyProfile() }

    override suspend fun getUser(userId: Long) = taigaApi.getUser(userId)

    override suspend fun getUserStats(userId: Long): Stats =
        withIO { taigaApi.getUserStats(userId) }

    override suspend fun getTeam() = withIO {
        val team = async { projectsApi.getProject(currentProjectId).members }
        val stats = async {
            taigaApi.getMemberStats(currentProjectId).run {
                // calculating total number of points for each id
                (closed_bugs.toList() + closed_tasks.toList() + created_bugs.toList() +
                        iocaine_tasks.toList() + wiki_changes.toList())
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
                    name = it.full_name_display,
                    role = it.role_name,
                    username = it.username,
                    totalPower = stat[it.id] ?: 0
                )
            }
        }
    }
}