package io.eugenethedev.taigamobile.userstories

import androidx.paging.PagingData
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import kotlinx.coroutines.flow.Flow

interface UserStoriesRepository {
    fun getUserStories(filters: FiltersData): Flow<PagingData<CommonTask>>
}
