package com.grappim.taigamobile.feature.issues.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.issues.dto.CreateIssueRequestDTO
import com.grappim.taigamobile.feature.issues.mapper.IssueMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemRemoteMediator
import com.grappim.taigamobile.feature.workitem.data.toDomain
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IssuesRepositoryImpl @Inject constructor(
    private val issuesApi: IssuesApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val issueMapper: IssueMapper,
    private val workItemApi: WorkItemApi,
    private val workItemMapper: WorkItemMapper,
    private val workItemDao: WorkItemDao
) : IssuesRepository {
    private var issuesPagingSource: IssuesPagingSource? = null

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override fun getIssuesPaging(filtersData: FiltersData, query: String): Flow<PagingData<WorkItem>> {
        val hasFilters = filtersData.filtersNumber > 0 || query.isNotBlank()
        if (hasFilters) {
            return Pager(
                PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = false
                )
            ) {
                IssuesPagingSource(
                    filters = filtersData,
                    taigaSessionStorage = taigaSessionStorage,
                    query = query,
                    workItemApi = workItemApi,
                    workItemMapper = workItemMapper
                ).also {
                    issuesPagingSource = it
                }
            }.flow
        }
        return taigaSessionStorage.currentProjectIdFlow.flatMapLatest { projectId ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = false
                ),
                remoteMediator = WorkItemRemoteMediator(
                    taskType = CommonTaskType.Issue,
                    workItemApi = workItemApi,
                    workItemDao = workItemDao,
                    workItemMapper = workItemMapper,
                    taigaSessionStorage = taigaSessionStorage
                ),
                pagingSourceFactory = { workItemDao.pagingSource(projectId, CommonTaskType.Issue) }
            ).flow.map { pagingData ->
                pagingData.map { entity -> entity.toDomain() }
            }
        }
    }

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
                project = taigaSessionStorage.getCurrentProjectId(),
                subject = title,
                description = description,
                milestone = sprintId
            )
        )
        return workItemMapper.toDomain(response, CommonTaskType.Issue)
    }
}
