package com.grappim.taigamobile.issues

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.grappim.taigamobile.data.repositories.commaString
import com.grappim.taigamobile.data.repositories.tagsCommaString
import com.grappim.taigamobile.data.repositories.toCommonTask
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.CommonTaskType
import com.grappim.taigamobile.domain.entities.FiltersData
import com.grappim.taigamobile.state.Session
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

class IssuesPagingSource(
    private val issuesApi: IssuesApi,
    private val filters: FiltersData,
    private val session: Session
) : PagingSource<Int, CommonTask>() {
    override fun getRefreshKey(state: PagingState<Int, CommonTask>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CommonTask> {
        try {
            val nextPageNumber = params.key ?: 1
            val response = issuesApi.getIssues(
                page = nextPageNumber,
                project = session.currentProjectId.value,
                query = filters.query,
                assignedIds = filters.assignees.commaString(),
                ownerIds = filters.createdBy.commaString(),
                priorities = filters.priorities.commaString(),
                severities = filters.severities.commaString(),
                types = filters.types.commaString(),
                statuses = filters.statuses.commaString(),
                roles = filters.roles.commaString(),
                tags = filters.tags.tagsCommaString()
            ).map { it.toCommonTask(CommonTaskType.Issue) }
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