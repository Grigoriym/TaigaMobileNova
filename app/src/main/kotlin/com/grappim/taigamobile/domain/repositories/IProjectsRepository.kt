package com.grappim.taigamobile.domain.repositories

import androidx.paging.PagingData
import com.grappim.taigamobile.domain.entities.Project
import kotlinx.coroutines.flow.Flow

interface IProjectsRepository {
    suspend fun fetchProjects(query: String):Flow<PagingData<Project>>
    suspend fun getMyProjects(): List<Project>
    suspend fun getUserProjects(userId: Long): List<Project>
}