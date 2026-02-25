package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.repo.FiltersRepository
import kotlinx.collections.immutable.ImmutableList

class FakeFiltersRepository : FiltersRepository {
    override suspend fun getFiltersData(
        commonTaskType: CommonTaskType,
        isCommonTaskFromBacklog: Boolean
    ): FiltersData {
        TODO("Not yet implemented")
    }

    override suspend fun getStatuses(commonTaskType: CommonTaskType): ImmutableList<Statuses> {
        TODO("Not yet implemented")
    }
}
