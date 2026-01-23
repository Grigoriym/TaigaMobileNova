package com.grappim.taigamobile.feature.filters.mapper

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.feature.filters.domain.model.filters.EpicsFilters
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.filters.RoleFilters
import com.grappim.taigamobile.feature.filters.domain.model.filters.StatusFilters
import com.grappim.taigamobile.feature.filters.domain.model.filters.TagFilters
import com.grappim.taigamobile.feature.filters.domain.model.filters.UsersFilters
import com.grappim.taigamobile.feature.filters.dto.FiltersDataResponseDTO
import com.grappim.taigamobile.utils.ui.fixNullColor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FiltersMapper @Inject constructor(@param:DefaultDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toDomain(response: FiltersDataResponseDTO): FiltersData = withContext(dispatcher) {
        FiltersData(
            assignees = response.assignedTo.filter { it.id != null }.map { dto ->
                UsersFilters(
                    id = dto.id,
                    name = dto.fullName,
                    count = dto.count
                )
            }.toImmutableList(),
            createdBy = response.owners.filter { it.id != null }.map { dto ->
                UsersFilters(
                    id = dto.id,
                    name = dto.fullName,
                    count = dto.count
                )
            }.toImmutableList(),
            priorities = response.priorities.orEmpty().map { dto ->
                StatusFilters(
                    id = dto.id,
                    name = dto.name,
                    color = dto.color.fixNullColor(),
                    count = dto.count
                )
            }.toImmutableList(),
            roles = response.roles.orEmpty().map { dto ->
                RoleFilters(
                    id = dto.id,
                    name = dto.name,
                    count = dto.count,
                    color = dto.color.fixNullColor()
                )
            }.toImmutableList(),
            severities = response.severities.orEmpty().map { dto ->
                StatusFilters(
                    id = dto.id,
                    name = dto.name,
                    color = dto.color.fixNullColor(),
                    count = dto.count
                )
            }.toImmutableList(),
            statuses = response.statuses.map { dto ->
                StatusFilters(
                    id = dto.id,
                    name = dto.name,
                    color = dto.color.fixNullColor(),
                    count = dto.count
                )
            }.toImmutableList(),
            tags = response.tags.orEmpty().map { dto ->
                TagFilters(
                    name = dto.name,
                    color = dto.color.fixNullColor(),
                    count = dto.count
                )
            }.toImmutableList(),
            types = response.types.orEmpty().map { dto ->
                StatusFilters(
                    id = dto.id,
                    name = dto.name,
                    color = dto.color.fixNullColor(),
                    count = dto.count
                )
            }.toImmutableList(),
            epics = response.epics.orEmpty().map { dto ->
                EpicsFilters(
                    id = dto.id,
                    name = dto.subject?.let { s -> "#${dto.ref} $s" }.orEmpty(),
                    count = dto.count
                )
            }.toImmutableList()
        )
    }
}
