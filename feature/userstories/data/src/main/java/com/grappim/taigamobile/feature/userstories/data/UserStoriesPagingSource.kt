package com.grappim.taigamobile.feature.userstories.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.api.tryCatchWithPagination
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.commaString
import com.grappim.taigamobile.core.domain.tagsCommaString
import com.grappim.taigamobile.core.storage.TaigaStorage
import kotlinx.coroutines.flow.first

class UserStoriesPagingSource(
    private val userStoriesApi: UserStoriesApi,
    private val filters: FiltersDataDTO,
    private val taigaStorage: TaigaStorage
) : PagingSource<Int, CommonTask>() {
    override fun getRefreshKey(state: PagingState<Int, CommonTask>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CommonTask> = tryCatchWithPagination(
        block = {
            val nextPageNumber = params.key ?: 1
            val response = userStoriesApi.getUserStoriesOld(
                project = taigaStorage.currentProjectIdFlow.first(),
                sprint = "null",
                page = nextPageNumber,
                query = filters.query,
                assignedIds = filters.assignees.commaString(),
                ownerIds = filters.createdBy.commaString(),
                roles = filters.roles.commaString(),
                statuses = filters.statuses.commaString(),
                epics = filters.epics.commaString(),
                tags = filters.tags.tagsCommaString()
            ).map { it.toCommonTask(CommonTaskType.UserStory) }

            LoadResult.Page(
                data = response,
                prevKey = null,
                nextKey = if (response.isNotEmpty()) nextPageNumber + 1 else null
            )
        },
        catchBlock = { e ->
            LoadResult.Error(e)
        },
        onPaginationEnd = {
            LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }
    )
}
