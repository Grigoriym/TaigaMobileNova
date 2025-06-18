package com.grappim.taigamobile.userstories

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

class UserStoriesPagingSource(
    private val userStoriesApi: UserStoriesApi,
    private val filters: FiltersData,
    private val session: Session,
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
            val response = userStoriesApi.getUserStories(
                project = session.currentProjectId.value,
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