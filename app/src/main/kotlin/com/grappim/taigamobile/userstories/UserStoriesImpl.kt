package com.grappim.taigamobile.userstories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.FiltersData
import com.grappim.taigamobile.state.Session
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
