package com.grappim.taigamobile.feature.epics.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.FiltersData
import kotlinx.coroutines.flow.Flow

interface EpicsRepository {
    fun getEpics(filters: FiltersData): Flow<PagingData<CommonTask>>
}
