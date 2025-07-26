package com.grappim.taigamobile.feature.projects.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.ProjectDTO
import kotlinx.coroutines.flow.Flow

interface ProjectsRepository {
    suspend fun fetchProjects(query: String): Flow<PagingData<ProjectDTO>>
    suspend fun getMyProjects(): List<ProjectDTO>
    suspend fun getUserProjects(userId: Long): List<ProjectDTO>
}
