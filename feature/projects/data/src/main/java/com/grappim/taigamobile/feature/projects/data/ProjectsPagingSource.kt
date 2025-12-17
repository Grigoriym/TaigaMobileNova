package com.grappim.taigamobile.feature.projects.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grappim.taigamobile.core.api.tryCatchWithPagination
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper

class ProjectsPagingSource(
    private val projectsApi: ProjectsApi,
    private val query: String,
    private val projectMapper: ProjectMapper
) : PagingSource<Int, Project>() {
    override fun getRefreshKey(state: PagingState<Int, Project>): Int? = state.anchorPosition?.let { anchorPosition ->
        val anchorPage = state.closestPageToPosition(anchorPosition)
        anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Project> = tryCatchWithPagination(
        block = {
            val nextPageNumber = params.key ?: 1
            val response = projectsApi.getProjects(
                query = query,
                page = nextPageNumber,
                pageSize = params.loadSize
            )
            val result = projectMapper.toListDomain(response)

            LoadResult.Page(
                data = result,
                prevKey = null,
                nextKey = if (result.isNotEmpty()) nextPageNumber + 1 else null
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
