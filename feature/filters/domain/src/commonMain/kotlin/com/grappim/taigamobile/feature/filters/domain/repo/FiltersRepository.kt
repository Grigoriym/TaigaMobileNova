package com.grappim.taigamobile.feature.filters.domain.repo

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import kotlinx.collections.immutable.ImmutableList

interface FiltersRepository {

    suspend fun getFiltersData(commonTaskType: CommonTaskType, isCommonTaskFromBacklog: Boolean = false): FiltersData

    suspend fun getStatuses(commonTaskType: CommonTaskType): ImmutableList<Statuses>
}