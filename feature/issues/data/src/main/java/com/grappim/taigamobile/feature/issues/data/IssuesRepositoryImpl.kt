package com.grappim.taigamobile.feature.issues.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.issues.domain.IssueTask
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IssuesRepositoryImpl @Inject constructor(
    private val issuesApi: IssuesApi,
    private val taigaStorage: TaigaStorage,
    private val commonTaskMapper: CommonTaskMapper,
    private val issueTaskMapper: IssueTaskMapper,
    private val workItemApi: WorkItemApi
) : IssuesRepository {
    private var issuesPagingSource: IssuesPagingSource? = null

    override fun getIssuesPaging(filtersDataDTO: FiltersDataDTO): Flow<PagingData<CommonTask>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        IssuesPagingSource(issuesApi, filtersDataDTO, taigaStorage).also {
            issuesPagingSource = it
        }
    }.flow

    override fun refreshIssues() {
        issuesPagingSource?.invalidate()
    }

    override suspend fun getIssue(id: Long, filtersData: FiltersData): IssueTask {
        val response = workItemApi.getWorkItemById(
            taskPath = WorkItemPathPlural(CommonTaskType.Issue),
            id = id
        )
        return issueTaskMapper.toDomain(resp = response, filters = filtersData)
    }

    override suspend fun createIssue(title: String, description: String, sprintId: Long?): CommonTaskResponse =
        issuesApi.createIssue(
            createIssueRequest = CreateIssueRequest(
                project = taigaStorage.currentProjectIdFlow.first(),
                subject = title,
                description = description,
                milestone = sprintId
            )
        )

    override suspend fun getIssues(
        isClosed: Boolean,
        assignedIds: String?,
        watcherId: Long?,
        project: Long?,
        sprint: Long?
    ): List<CommonTask> = issuesApi.getIssues(
        assignedIds = assignedIds,
        isClosed = isClosed,
        watcherId = watcherId,
        project = project,
        sprint = sprint
    ).map { commonTaskMapper.toDomain(it, CommonTaskType.Issue) }
}
