package com.grappim.taigamobile.feature.sprint.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.storage.Session
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

class SprintPagingSource(
    private val sprintApi: SprintApi,
    private val isClosed: Boolean,
    private val session: Session
) : PagingSource<Int, Sprint>() {
    override fun getRefreshKey(state: PagingState<Int, Sprint>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Sprint> {
        try {
            val nextPageNumber = params.key ?: 1
            val response =
                sprintApi.getSprints(
                    project = session.currentProjectId.value,
                    page = nextPageNumber,
                    isClosed = isClosed
                ).map { it.toSprint() }
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
                    nextKey = null
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
