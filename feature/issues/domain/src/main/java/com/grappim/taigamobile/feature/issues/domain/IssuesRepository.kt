package com.grappim.taigamobile.feature.issues.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface IssuesRepository {

    suspend fun getIssue(id: Long, filtersData: FiltersData): Issue

    fun getIssuesPaging(filtersData: FiltersData, query: String): Flow<PagingData<WorkItem>>

    fun refreshIssues()

    suspend fun createIssue(title: String, description: String, sprintId: Long?): WorkItem
}
