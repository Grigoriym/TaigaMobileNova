package com.grappim.taigamobile.feature.userstories.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserStoriesRepositoryImpl @Inject constructor(
    private val userStoriesApi: UserStoriesApi,
    private val session: Session
) : UserStoriesRepository {
    override fun getUserStories(filters: FiltersData): Flow<PagingData<CommonTask>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        UserStoriesPagingSource(userStoriesApi, filters, session)
    }.flow
}
