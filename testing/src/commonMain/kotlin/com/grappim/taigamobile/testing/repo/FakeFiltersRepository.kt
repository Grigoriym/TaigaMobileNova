package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.repo.FiltersRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class FakeFiltersRepository : FiltersRepository {

    var filtersDataResult: FiltersData? = null
    var filtersDataThrows: Throwable? = null
    var getFiltersDataCallCount = 0
    var statusesResult: ImmutableList<Statuses> = persistentListOf()
    var statusesThrows: Throwable? = null

    override suspend fun getFiltersData(
        commonTaskType: CommonTaskType,
        isCommonTaskFromBacklog: Boolean
    ): FiltersData {
        getFiltersDataCallCount++
        filtersDataThrows?.let { throw it }
        return filtersDataResult ?: error("filtersDataResult not set")
    }

    override suspend fun getStatuses(commonTaskType: CommonTaskType): ImmutableList<Statuses> {
        statusesThrows?.let { throw it }
        return statusesResult
    }
}
