package com.grappim.taigamobile.feature.projects.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grappim.taigamobile.core.api.tryCatchWithPagination
import com.grappim.taigamobile.core.domain.ProjectDTO

class ProjectsPagingSource(private val projectsApi: ProjectsApi, private val query: String) :
    PagingSource<Int, ProjectDTO>() {
    override fun getRefreshKey(state: PagingState<Int, ProjectDTO>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProjectDTO> =
        tryCatchWithPagination(
            block = {
                val nextPageNumber = params.key ?: 1
                val response = projectsApi.getProjects(
                    query = query,
                    page = nextPageNumber,
                    pageSize = params.loadSize
                )

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
