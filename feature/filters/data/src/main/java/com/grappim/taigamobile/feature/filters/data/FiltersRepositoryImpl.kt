package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.api.withIO
import com.grappim.taigamobile.core.domain.CommonTaskPathPlural
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.domain.toStatus
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class FiltersRepositoryImpl @Inject constructor(
    private val filtersApi: FiltersApi,
    private val taigaStorage: TaigaStorage,
    private val filtersMapper: FiltersMapper
) : FiltersRepository {

    override suspend fun getFiltersData(commonTaskType: CommonTaskType): FiltersData {
        val response = filtersApi.getCommonTaskFiltersData(
            taskPath = CommonTaskPathPlural(commonTaskType),
            project = taigaStorage.currentProjectIdFlow.first()
        )
        return filtersMapper.toDomain(response)
    }

    override suspend fun getFiltersDataResult(commonTaskType: CommonTaskType): Result<FiltersData> =
        resultOf {
            getFiltersData(commonTaskType)
        }

    override suspend fun getFiltersDataOld(
        commonTaskType: CommonTaskType,
        isCommonTaskFromBacklog: Boolean
    ): FiltersDataDTO {
        val result = filtersApi.getCommonTaskFiltersData(
            taskPath = CommonTaskPathPlural(commonTaskType),
            project = taigaStorage.currentProjectIdFlow.first(),
            milestone = if (isCommonTaskFromBacklog) "null" else null
        )
        return filtersMapper.toFiltersDataDTO(result)
    }

    override suspend fun getFiltersDataResultOld(
        commonTaskType: CommonTaskType,
        isCommonTaskFromBacklog: Boolean
    ): Result<FiltersDataDTO> = resultOf {
        getFiltersDataOld(
            commonTaskType = commonTaskType,
            isCommonTaskFromBacklog = isCommonTaskFromBacklog
        )
    }

    override suspend fun getStatuses(commonTaskType: CommonTaskType) =
        getFiltersDataOld(commonTaskType).statuses.map { it.toStatus(StatusType.Status) }

    override suspend fun getStatusByType(commonTaskType: CommonTaskType, statusType: StatusType) =
        withIO {
            if (commonTaskType != CommonTaskType.Issue && statusType != StatusType.Status) {
                throw UnsupportedOperationException("Cannot get $statusType for $commonTaskType")
            }

            getFiltersDataOld(commonTaskType).let {
                when (statusType) {
                    StatusType.Status -> it.statuses.map { it.toStatus(statusType) }
                    StatusType.Type -> it.types.map { it.toStatus(statusType) }
                    StatusType.Severity -> it.severities.map { it.toStatus(statusType) }
                    StatusType.Priority -> it.priorities.map { it.toStatus(statusType) }
                }
            }
        }

    override suspend fun getAllTags(commonTaskType: CommonTaskType) =
        getFiltersDataOld(commonTaskType).tags.map { Tag(it.name, it.color) }
}
