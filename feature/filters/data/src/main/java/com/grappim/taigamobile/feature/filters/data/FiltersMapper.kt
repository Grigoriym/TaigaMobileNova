package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.core.domain.EpicsFilter
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.RolesFilter
import com.grappim.taigamobile.core.domain.StatusesFilter
import com.grappim.taigamobile.core.domain.TagsFilter
import com.grappim.taigamobile.core.domain.UsersFilter
import com.grappim.taigamobile.feature.filters.domain.model.AssignedTo
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Owner
import com.grappim.taigamobile.feature.filters.domain.model.Priority
import com.grappim.taigamobile.feature.filters.domain.model.Role
import com.grappim.taigamobile.feature.filters.domain.model.Severity
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.filters.domain.model.Type
import com.grappim.taigamobile.utils.ui.fixNullColor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FiltersMapper @Inject constructor(@param:DefaultDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toDomain(response: FiltersDataResponse): FiltersData = withContext(dispatcher) {
        FiltersData(
            assignedTo = response.assignedTo.filter { it.id != null }.map { dto ->
                AssignedTo(
                    id = dto.id!!,
                    fullName = dto.fullName,
                    count = dto.count
                )
            }.toImmutableList(),
            owners = response.owners.filter { it.id != null }.map { dto ->
                Owner(
                    id = dto.id!!,
                    fullName = dto.fullName,
                    count = dto.count
                )
            }.toImmutableList(),
            priorities = response.priorities.orEmpty().map { dto ->
                Priority(
                    id = dto.id,
                    name = dto.name,
                    color = dto.color.fixNullColor()
                )
            }.toImmutableList(),
            roles = response.roles.orEmpty().map { dto ->
                Role(
                    id = dto.id,
                    name = dto.name,
                    count = dto.count,
                    color = dto.color.fixNullColor(),
                    order = dto.order
                )
            }.toImmutableList(),
            severities = response.severities.orEmpty().map { dto ->
                Severity(
                    id = dto.id,
                    name = dto.name,
                    color = dto.color.fixNullColor()
                )
            }.toImmutableList(),
            statuses = response.statuses.map { dto ->
                Status(
                    id = dto.id,
                    name = dto.name,
                    color = dto.color.fixNullColor()
                )
            }.toImmutableList(),
            tags = response.tags.orEmpty().map { dto ->
                Tag(
                    name = dto.name,
                    color = dto.color.fixNullColor()
                )
            }.toImmutableList(),
            types = response.types.orEmpty().map { dto ->
                Type(
                    id = dto.id,
                    name = dto.name,
                    color = dto.color.fixNullColor()
                )
            }.toImmutableList()
        )
    }

    suspend fun toFiltersDataDTO(response: FiltersDataResponse): FiltersDataDTO = withContext(dispatcher) {
        FiltersDataDTO(
            assignees = response.assignedTo.map {
                UsersFilter(
                    id = it.id,
                    name = it.fullName,
                    count = it.count
                )
            },
            roles = response.roles.orEmpty().map {
                RolesFilter(
                    id = it.id,
                    name = it.name,
                    count = it.count
                )
            },
            tags = response.tags.orEmpty().map {
                TagsFilter(
                    name = it.name,
                    color = it.color.fixNullColor(),
                    count = it.count
                )
            },
            statuses = response.statuses.map {
                StatusesFilter(
                    id = it.id,
                    color = it.color.fixNullColor(),
                    name = it.name,
                    count = it.count
                )
            },
            createdBy = response.owners.map {
                UsersFilter(
                    id = it.id,
                    name = it.fullName,
                    count = it.count
                )
            },
            priorities = response.priorities.orEmpty().map {
                StatusesFilter(
                    id = it.id,
                    color = it.color.fixNullColor(),
                    name = it.name,
                    count = it.count
                )
            },
            severities = response.severities.orEmpty().map {
                StatusesFilter(
                    id = it.id,
                    color = it.color.fixNullColor(),
                    name = it.name,
                    count = it.count
                )
            },
            types = response.types.orEmpty().map {
                StatusesFilter(
                    id = it.id,
                    color = it.color.fixNullColor(),
                    name = it.name,
                    count = it.count
                )
            },
            epics = response.epics.orEmpty().map {
                EpicsFilter(
                    id = it.id,
                    name = it.subject?.let { s -> "#${it.ref} $s" }.orEmpty(),
                    count = it.count
                )
            }
        )
    }
}
