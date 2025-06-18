package io.eugenethedev.taigamobile.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.eugenethedev.taigamobile.domain.entities.Project
import io.eugenethedev.taigamobile.domain.repositories.IProjectsRepository
import io.eugenethedev.taigamobile.projectselector.ProjectsApi
import io.eugenethedev.taigamobile.projectselector.ProjectsPagingSource
import io.eugenethedev.taigamobile.state.Session
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProjectsRepository @Inject constructor(
    private val projectsApi: ProjectsApi,
    private val session: Session
) : IProjectsRepository {
    override suspend fun fetchProjects(query: String): Flow<PagingData<Project>> =
        Pager(
            PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
            )
        ) {
            ProjectsPagingSource(projectsApi, query)
        }.flow

    override suspend fun getMyProjects() =
        projectsApi.getProjects(memberId = session.currentUserId.value)

    override suspend fun getUserProjects(userId: Long) =
        projectsApi.getProjects(memberId = userId)
}
