package io.eugenethedev.taigamobile.epics

import androidx.paging.PagingData
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import kotlinx.coroutines.flow.Flow

interface EpicsRepository {
    fun getEpics(filters: FiltersData): Flow<PagingData<CommonTask>>
}
