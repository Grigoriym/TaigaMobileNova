package com.grappim.taigamobile.feature.projects.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProjectsRepositoryImpl @Inject constructor(
    private val projectsApi: ProjectsApi,
    private val session: Session,
    private val projectMapper: ProjectMapper
) : ProjectsRepository {
    override suspend fun fetchProjects(query: String): Flow<PagingData<Project>> = Pager(
        PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        )
    ) {
        ProjectsPagingSource(projectsApi, query, projectMapper)
    }.flow

    override suspend fun getMyProjects(): ImmutableList<Project> {
        val response = projectsApi.getProjects(memberId = session.userId)
        return projectMapper.toListDomain(response)
    }

    override suspend fun getUserProjects(userId: Long): ImmutableList<Project> {
        val response = projectsApi.getProjects(memberId = userId)
        return projectMapper.toListDomain(response)
    }
}
