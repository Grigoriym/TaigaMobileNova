package com.grappim.taigamobile.feature.userstories.data

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

class UserStoriesPagingSource(
    private val filters: FiltersData,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val query: String,
    private val workItemMapper: WorkItemMapper,
    private val workItemApi: WorkItemApi
) : PagingSource<Int, WorkItem>() {
    override fun getRefreshKey(state: PagingState<Int, WorkItem>): Int? = state.anchorPosition?.let { anchorPosition ->
        val anchorPage = state.closestPageToPosition(anchorPosition)
        anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, WorkItem> = defaultTryCatch(
        block = {
            val nextPageNumber = params.key ?: 1
            val response = workItemApi.getWorkItemsPagination(
                taskPath = WorkItemPathPlural(CommonTaskType.UserStory),
                project = taigaSessionStorage.getCurrentProjectId(),
                sprint = "null",
                page = nextPageNumber,
                pageSize = params.loadSize,
                query = query,
                assignedIds = filters.assignees.commaString(),
                ownerIds = filters.createdBy.commaString(),
                roles = filters.roles.commaString(),
                statuses = filters.statuses.commaString(),
                epics = filters.epics.commaString(),
                tags = filters.tags.tagsCommaString()
            )
            val result = workItemMapper.toDomainList(response.body() ?: emptyList(), CommonTaskType.UserStory)

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
