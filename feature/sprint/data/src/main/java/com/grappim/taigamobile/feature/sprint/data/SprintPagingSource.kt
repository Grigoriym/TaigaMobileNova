package com.grappim.taigamobile.feature.sprint.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grappim.taigamobile.core.api.defaultTryCatch
import com.grappim.taigamobile.core.api.hasNextPage
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import kotlinx.coroutines.flow.first

class SprintPagingSource(
    private val sprintApi: SprintApi,
    private val isClosed: Boolean,
    private val taigaStorage: TaigaStorage,
    private val sprintMapper: SprintMapper
) : PagingSource<Int, Sprint>() {
    override fun getRefreshKey(state: PagingState<Int, Sprint>): Int? = state.anchorPosition?.let { anchorPosition ->
        val anchorPage = state.closestPageToPosition(anchorPosition)
        anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Sprint> = defaultTryCatch(
        block = {
            val nextPageNumber = params.key ?: 1
            val response = sprintApi.getSprintsPaging(
                project = taigaStorage.currentProjectIdFlow.first(),
                page = nextPageNumber,
                isClosed = isClosed
            )
            val result = sprintMapper.toDomainList(response.body() ?: emptyList())
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
