package com.grappim.taigamobile.feature.issues.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.FiltersData
import kotlinx.coroutines.flow.Flow

interface IssuesRepository {
    fun getIssuesPaging(filtersData: FiltersData): Flow<PagingData<CommonTask>>

    fun refreshIssues()

    suspend fun createIssue(title: String, description: String, sprintId: Long?): CommonTaskResponse

    suspend fun getIssues(
        isClosed: Boolean = false,
        assignedIds: String? = null,
        watcherId: Long? = null
    ): List<CommonTask>
}
