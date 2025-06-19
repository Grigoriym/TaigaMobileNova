package com.grappim.taigamobile.feature.userstories.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.FiltersData
import kotlinx.coroutines.flow.Flow

interface UserStoriesRepository {
    fun getUserStories(filters: FiltersData): Flow<PagingData<CommonTask>>
}
