package com.grappim.taigamobile.feature.sprint.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grappim.taigamobile.core.api.tryCatchWithPagination
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.storage.TaigaStorage
import kotlinx.coroutines.flow.first

class SprintPagingSource(
    private val sprintApi: SprintApi,
    private val isClosed: Boolean,
    private val taigaStorage: TaigaStorage
) : PagingSource<Int, Sprint>() {
    override fun getRefreshKey(state: PagingState<Int, Sprint>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Sprint> =
        tryCatchWithPagination(
            block = {
                val nextPageNumber = params.key ?: 1
                val response =
                    sprintApi.getSprints(
                        project = taigaStorage.currentProjectIdFlow.first(),
                        page = nextPageNumber,
                        isClosed = isClosed
                    ).map { it.toSprint() }
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
