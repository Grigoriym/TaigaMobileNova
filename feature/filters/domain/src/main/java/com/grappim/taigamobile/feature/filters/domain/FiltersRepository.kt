package com.grappim.taigamobile.feature.filters.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.Tag

interface FiltersRepository {
    suspend fun getFiltersData(
        commonTaskType: CommonTaskType,
        isCommonTaskFromBacklog: Boolean = false
    ): FiltersData

    suspend fun getFiltersDataResult(
        commonTaskType: CommonTaskType,
        isCommonTaskFromBacklog: Boolean = false
    ): Result<FiltersData>

    suspend fun getStatuses(commonTaskType: CommonTaskType): List<Status>
    suspend fun getStatusByType(
        commonTaskType: CommonTaskType,
        statusType: StatusType
    ): List<Status>

    suspend fun getAllTags(commonTaskType: CommonTaskType): List<Tag>
}
