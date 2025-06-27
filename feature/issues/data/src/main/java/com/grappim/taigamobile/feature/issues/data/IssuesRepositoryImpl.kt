package com.grappim.taigamobile.feature.issues.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IssuesRepositoryImpl @Inject constructor(
    private val issuesApi: IssuesApi,
    private val taigaStorage: TaigaStorage
) : IssuesRepository {
    private var issuesPagingSource: IssuesPagingSource? = null

    override fun getIssuesPaging(filtersData: FiltersData): Flow<PagingData<CommonTask>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        IssuesPagingSource(issuesApi, filtersData, taigaStorage).also { issuesPagingSource = it }
    }.flow

    override fun refreshIssues() {
        issuesPagingSource?.invalidate()
    }

    override suspend fun createIssue(
        title: String,
        description: String,
        sprintId: Long?
    ): CommonTaskResponse = issuesApi.createIssue(
        createIssueRequest = CreateIssueRequest(
            taigaStorage.currentProjectIdFlow.first(),
            title,
            description,
            sprintId
        )
    )

    override suspend fun getIssues(
        isClosed: Boolean,
        assignedIds: String?,
        watcherId: Long?
    ): List<CommonTask> =
        issuesApi.getIssues(assignedIds = assignedIds, isClosed = isClosed, watcherId = watcherId)
            .map { it.toCommonTask(CommonTaskType.Issue) }
}
