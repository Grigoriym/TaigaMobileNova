package com.grappim.taigamobile.epics

import androidx.paging.PagingData
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.FiltersData
import kotlinx.coroutines.flow.Flow

interface EpicsRepository {
    fun getEpics(filters: FiltersData): Flow<PagingData<CommonTask>>
}
