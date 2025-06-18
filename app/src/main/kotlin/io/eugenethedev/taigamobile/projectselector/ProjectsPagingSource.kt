package io.eugenethedev.taigamobile.projectselector

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.eugenethedev.taigamobile.domain.entities.Project
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class ProjectsPagingSource(
    private val projectsApi: ProjectsApi,
    private val query: String,
) : PagingSource<Int, Project>() {
    override fun getRefreshKey(state: PagingState<Int, Project>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Project> {
        try {
            val nextPageNumber = params.key ?: 1
            val response = projectsApi.getProjects(
                query = query,
                page = nextPageNumber,
                pageSize = params.loadSize
            )

            return LoadResult.Page(
                data = response,
                prevKey = null,
                nextKey = if (response.isNotEmpty()) nextPageNumber + 1 else null
            )
        } catch (e: HttpException) {
            if (e.code() == 404) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null,
                )
            }
           return LoadResult.Error(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}
