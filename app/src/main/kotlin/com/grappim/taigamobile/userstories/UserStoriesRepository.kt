package com.grappim.taigamobile.userstories

import androidx.paging.PagingData
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.FiltersData
import kotlinx.coroutines.flow.Flow

interface UserStoriesRepository {
    fun getUserStories(filters: FiltersData): Flow<PagingData<CommonTask>>
}
