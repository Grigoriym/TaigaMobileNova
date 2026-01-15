package com.grappim.taigamobile.feature.projects.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.projects.domain.ProjectSimple
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectsRepositoryImpl @Inject constructor(
    private val projectsApi: ProjectsApi,
    private val projectMapper: ProjectMapper,
    private val projectDao: ProjectDao,
    private val taigaSessionStorage: TaigaSessionStorage
) : ProjectsRepository {
    override suspend fun fetchProjects(query: String): Flow<PagingData<Project>> = Pager(
        PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        )
    ) {
        ProjectsPagingSource(
            projectsApi = projectsApi,
            query = query,
            projectMapper = projectMapper,
            taigaSessionStorage = taigaSessionStorage
        )
    }.flow

    override suspend fun getMyProjects(): ImmutableList<Project> {
        val response = projectsApi.getProjects(memberId = taigaSessionStorage.requireUserId())
        return projectMapper.toListDomain(response)
    }

    override suspend fun getUserProjects(userId: Long): ImmutableList<Project> {
        val response = projectsApi.getProjects(memberId = userId)
        return projectMapper.toListDomain(response)
    }

    override suspend fun fetchAndSaveProjectInfo() {
        val response = projectsApi.getProjects(
            memberId = taigaSessionStorage.requireUserId()
        ).find { it.id == taigaSessionStorage.getCurrentProjectId() } ?: error("Something is not right")
        val entity = projectMapper.toEntity(response)
        projectDao.insert(entity)
    }

    override suspend fun saveProject(project: Project) {
        val entity = projectMapper.toEntity(project)
        projectDao.insert(entity)
    }

    override suspend fun getCurrentProjectSimple(): ProjectSimple {
        val currentProjectId = taigaSessionStorage.getCurrentProjectId()
        val entity = projectDao.getProjectById(currentProjectId)
        return projectMapper.toProjectSimple(entity)
    }

    override fun getCurrentProjectFlow(): Flow<ProjectSimple> = taigaSessionStorage.currentProjectIdFlow
        .flatMapLatest { projectId ->
            projectDao.getProjectByIdFlow(projectId)
        }
        .filterNotNull()
        .map { entity ->
            projectMapper.toProjectSimple(entity)
        }

    override suspend fun getPermissions(): ImmutableList<TaigaPermission> = getCurrentProjectSimple().myPermissions
}
