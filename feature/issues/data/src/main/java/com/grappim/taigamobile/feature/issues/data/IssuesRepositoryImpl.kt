package com.grappim.taigamobile.feature.issues.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.issues.dto.CreateIssueRequestDTO
import com.grappim.taigamobile.feature.issues.mapper.IssueMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IssuesRepositoryImpl @Inject constructor(
    private val issuesApi: IssuesApi,
    private val taigaStorage: TaigaStorage,
    private val issueMapper: IssueMapper,
    private val workItemApi: WorkItemApi,
    private val workItemMapper: WorkItemMapper
) : IssuesRepository {
    private var issuesPagingSource: IssuesPagingSource? = null

    override fun getIssuesPaging(filtersData: FiltersData, query: String): Flow<PagingData<WorkItem>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        IssuesPagingSource(
            filters = filtersData,
            taigaStorage = taigaStorage,
            query = query,
            workItemApi = workItemApi,
            workItemMapper = workItemMapper
        ).also {
            issuesPagingSource = it
        }
    }.flow

    override fun refreshIssues() {
        issuesPagingSource?.invalidate()
    }

    override suspend fun getIssue(id: Long, filtersData: FiltersData): Issue {
        val response = workItemApi.getWorkItemById(
            taskPath = WorkItemPathPlural(CommonTaskType.Issue),
            id = id
        )
        return issueMapper.toDomain(resp = response, filters = filtersData)
    }

    override suspend fun createIssue(title: String, description: String, sprintId: Long?): WorkItem {
        val response = issuesApi.createIssue(
            createIssueRequest = CreateIssueRequestDTO(
                project = taigaStorage.currentProjectIdFlow.first(),
                subject = title,
                description = description,
                milestone = sprintId
            )
        )
        return workItemMapper.toDomain(response, CommonTaskType.Issue)
    }
}
