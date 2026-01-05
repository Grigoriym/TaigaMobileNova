package com.grappim.taigamobile.feature.filters.mapper

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.feature.filters.domain.model.filters.EpicsFilters
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.filters.RoleFilters
import com.grappim.taigamobile.feature.filters.domain.model.filters.StatusFilters
import com.grappim.taigamobile.feature.filters.domain.model.filters.TagFilters
import com.grappim.taigamobile.feature.filters.domain.model.filters.UsersFilters
import com.grappim.taigamobile.feature.filters.dto.EpicsFilterDTO
import com.grappim.taigamobile.feature.filters.dto.FiltersDataDTO
import com.grappim.taigamobile.feature.filters.dto.FiltersDataResponseDTO
import com.grappim.taigamobile.feature.filters.dto.RolesFilterDTO
import com.grappim.taigamobile.feature.filters.dto.StatusesFilterDTO
import com.grappim.taigamobile.feature.filters.dto.TagsFilterDTO
import com.grappim.taigamobile.feature.filters.dto.UsersFilterDTO
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

    suspend fun toFiltersDataDTO(response: FiltersDataResponseDTO): FiltersDataDTO = withContext(dispatcher) {
        FiltersDataDTO(
            assignees = response.assignedTo.map {
                UsersFilterDTO(
                    id = it.id,
                    name = it.fullName,
                    count = it.count
                )
            },
            roles = response.roles.orEmpty().map {
                RolesFilterDTO(
                    id = it.id,
                    name = it.name,
                    count = it.count
                )
            },
            tags = response.tags.orEmpty().map {
                TagsFilterDTO(
                    name = it.name,
                    color = it.color.fixNullColor(),
                    count = it.count
                )
            },
            statuses = response.statuses.map {
                StatusesFilterDTO(
                    id = it.id,
                    color = it.color.fixNullColor(),
                    name = it.name,
                    count = it.count
                )
            },
            createdBy = response.owners.map {
                UsersFilterDTO(
                    id = it.id,
                    name = it.fullName,
                    count = it.count
                )
            },
            priorities = response.priorities.orEmpty().map {
                StatusesFilterDTO(
                    id = it.id,
                    color = it.color.fixNullColor(),
                    name = it.name,
                    count = it.count
                )
            },
            severities = response.severities.orEmpty().map {
                StatusesFilterDTO(
                    id = it.id,
                    color = it.color.fixNullColor(),
                    name = it.name,
                    count = it.count
                )
            },
            types = response.types.orEmpty().map {
                StatusesFilterDTO(
                    id = it.id,
                    color = it.color.fixNullColor(),
                    name = it.name,
                    count = it.count
                )
            },
            epics = response.epics.orEmpty().map {
                EpicsFilterDTO(
                    id = it.id,
                    name = it.subject?.let { s -> "#${it.ref} $s" }.orEmpty(),
                    count = it.count
                )
            }
        )
    }
}
