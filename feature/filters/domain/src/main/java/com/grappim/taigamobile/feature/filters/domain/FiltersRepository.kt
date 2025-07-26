package com.grappim.taigamobile.feature.filters.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData

interface FiltersRepository {

    suspend fun getFiltersData(commonTaskType: CommonTaskType): FiltersData

    suspend fun getFiltersDataResult(commonTaskType: CommonTaskType): Result<FiltersData>

    suspend fun getFiltersDataOld(
        commonTaskType: CommonTaskType,
        isCommonTaskFromBacklog: Boolean = false
    ): FiltersDataDTO

    suspend fun getFiltersDataResultOld(
        commonTaskType: CommonTaskType,
        isCommonTaskFromBacklog: Boolean = false
    ): Result<FiltersDataDTO>

    suspend fun getStatuses(commonTaskType: CommonTaskType): List<StatusOld>
    suspend fun getStatusByType(
        commonTaskType: CommonTaskType,
        statusType: StatusType
    ): List<StatusOld>

    suspend fun getAllTags(commonTaskType: CommonTaskType): List<Tag>
}
