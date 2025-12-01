package com.grappim.taigamobile.feature.issues.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.coroutines.flow.Flow

interface IssuesRepository {

    suspend fun getIssue(id: Long, filtersData: FiltersData): Issue

    fun getIssuesPaging(filtersDataDTO: FiltersDataDTO): Flow<PagingData<CommonTask>>

    fun refreshIssues()

    suspend fun createIssue(title: String, description: String, sprintId: Long?): CommonTaskResponse

    suspend fun getIssues(
        isClosed: Boolean = false,
        assignedIds: String? = null,
        watcherId: Long? = null,
        project: Long? = null,
        sprint: Long? = null
    ): List<CommonTask>
}
