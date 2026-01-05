package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.mapper.FiltersMapper
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class FiltersRepositoryImpl @Inject constructor(
    private val filtersApi: FiltersApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val filtersMapper: FiltersMapper
) : FiltersRepository {

    override suspend fun getFiltersData(commonTaskType: CommonTaskType, isCommonTaskFromBacklog: Boolean): FiltersData {
        val response = filtersApi.getCommonTaskFiltersData(
            taskPath = WorkItemPathPlural(commonTaskType),
            project = taigaSessionStorage.getCurrentProjectId(),
            milestone = if (isCommonTaskFromBacklog) "null" else null
        )
        return filtersMapper.toDomain(response)
    }

    override suspend fun getStatuses(commonTaskType: CommonTaskType): ImmutableList<Statuses> {
        val filtersData = getFiltersData(commonTaskType)
        return filtersData.statuses.map { filterStatus ->
            Status(
                color = filterStatus.color,
                id = filterStatus.id,
                name = filterStatus.name
            )
        }.toImmutableList()
    }
}
