package com.grappim.taigamobile.feature.projects.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProjectsRepositoryImpl @Inject constructor(
    private val projectsApi: ProjectsApi,
    private val session: Session
) : ProjectsRepository {
    override suspend fun fetchProjects(query: String): Flow<PagingData<Project>> = Pager(
        PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        )
    ) {
        ProjectsPagingSource(projectsApi, query)
    }.flow

    override suspend fun getMyProjects(): List<Project> =
        projectsApi.getProjects(memberId = session.userId)

    override suspend fun getUserProjects(userId: Long): List<Project> =
        projectsApi.getProjects(memberId = userId)
}
