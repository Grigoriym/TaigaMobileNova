package io.eugenethedev.taigamobile.userstories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import io.eugenethedev.taigamobile.state.Session
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserStoriesImpl @Inject constructor(
    private val userStoriesApi: UserStoriesApi,
    private val session: Session
) : UserStoriesRepository {
    override fun getUserStories(filters: FiltersData): Flow<PagingData<CommonTask>> =
        Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            )
        ) {
            UserStoriesPagingSource(userStoriesApi, filters, session)
        }.flow
}
