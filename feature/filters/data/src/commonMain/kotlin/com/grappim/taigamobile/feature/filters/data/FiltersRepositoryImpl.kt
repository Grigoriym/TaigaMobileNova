package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.KmpTaigaSessionStorage
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.repo.FiltersRepository
import com.grappim.taigamobile.feature.filters.mapper.FiltersMapper
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Single

@Single(binds = [FiltersRepository::class])
class FiltersRepositoryImpl(
    private val filtersApi: FiltersApi,
    private val taigaSessionStorage: KmpTaigaSessionStorage,
    private val filtersMapper: FiltersMapper
) : FiltersRepository {

    override suspend fun getFiltersData(commonTaskType: CommonTaskType, isCommonTaskFromBacklog: Boolean): FiltersData {
        val response = filtersApi.getCommonTaskFiltersData(
            taskPath = commonTaskType.getPluralPath(),
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
