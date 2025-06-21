package com.grappim.taigamobile.feature.projects.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.Project
import kotlinx.coroutines.flow.Flow

interface ProjectsRepository {
    suspend fun fetchProjects(query: String): Flow<PagingData<Project>>
    suspend fun getMyProjects(): List<Project>
    suspend fun getUserProjects(userId: Long): List<Project>
}
