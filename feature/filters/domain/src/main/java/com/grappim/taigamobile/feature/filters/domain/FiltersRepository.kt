package com.grappim.taigamobile.feature.filters.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import kotlinx.collections.immutable.ImmutableList

interface FiltersRepository {

    suspend fun getFiltersData(commonTaskType: CommonTaskType, isCommonTaskFromBacklog: Boolean = false): FiltersData

    suspend fun getStatuses(commonTaskType: CommonTaskType): ImmutableList<Statuses>
}
