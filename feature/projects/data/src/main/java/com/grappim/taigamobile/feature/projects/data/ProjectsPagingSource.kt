package com.grappim.taigamobile.feature.projects.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grappim.taigamobile.core.api.defaultTryCatch
import com.grappim.taigamobile.core.api.hasNextPage
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper

class ProjectsPagingSource(
    private val projectsApi: ProjectsApi,
    private val query: String,
    private val projectMapper: ProjectMapper,
    private val taigaSessionStorage: TaigaSessionStorage
) : PagingSource<Int, Project>() {
    override fun getRefreshKey(state: PagingState<Int, Project>): Int? = state.anchorPosition?.let { anchorPosition ->
        val anchorPage = state.closestPageToPosition(anchorPosition)
        anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Project> = defaultTryCatch(
        block = {
            val nextPageNumber = params.key ?: 1
            val response = projectsApi.getProjectsPaging(
                query = query,
                page = nextPageNumber,
                pageSize = params.loadSize,
                memberId = taigaSessionStorage.requireUserId()
            )
            val result = projectMapper.toListDomain(response.body() ?: emptyList())

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
