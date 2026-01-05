package com.grappim.taigamobile.feature.issues.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grappim.taigamobile.core.api.defaultTryCatch
import com.grappim.taigamobile.core.api.hasNextPage
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.filters.domain.commaString
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.domain.tagsCommaString
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper

class IssuesPagingSource(
    private val filters: FiltersData,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val query: String,
    private val workItemApi: WorkItemApi,
    private val workItemMapper: WorkItemMapper
) : PagingSource<Int, WorkItem>() {
    override fun getRefreshKey(state: PagingState<Int, WorkItem>): Int? = state.anchorPosition?.let { anchorPosition ->
        val anchorPage = state.closestPageToPosition(anchorPosition)
        anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, WorkItem> = defaultTryCatch(
        block = {
            val nextPageNumber = params.key ?: 1
            val response = workItemApi.getWorkItemsPagination(
                taskPath = WorkItemPathPlural(CommonTaskType.Issue),
                page = nextPageNumber,
                project = taigaSessionStorage.getCurrentProjectId(),
                query = query,
                assignedIds = filters.assignees.commaString(),
                ownerIds = filters.createdBy.commaString(),
                priorities = filters.priorities.commaString(),
                severities = filters.severities.commaString(),
                types = filters.types.commaString(),
                statuses = filters.statuses.commaString(),
                roles = filters.roles.commaString(),
                tags = filters.tags.tagsCommaString()
            )
            val result = workItemMapper.toDomainList(
                dtos = response.body() ?: emptyList(),
                taskType = CommonTaskType.Issue
            )
            LoadResult.Page(
                data = result,
                prevKey = null,
                nextKey = if (response.hasNextPage()) nextPageNumber + 1 else null
            )
        },
        catchBlock = { e ->
            LoadResult.Error(e)
        }
    )
}
