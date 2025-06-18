package com.grappim.taigamobile.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.domain.entities.Project
import com.grappim.taigamobile.domain.repositories.IProjectsRepository
import com.grappim.taigamobile.projectselector.ProjectsApi
import com.grappim.taigamobile.projectselector.ProjectsPagingSource
import com.grappim.taigamobile.state.Session
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
