package com.grappim.taigamobile.feature.projects.domain

import androidx.paging.PagingData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface ProjectsRepository {
    suspend fun fetchProjects(query: String): Flow<PagingData<Project>>
    suspend fun getMyProjects(): ImmutableList<Project>
    suspend fun getUserProjects(userId: Long): ImmutableList<Project>
}
