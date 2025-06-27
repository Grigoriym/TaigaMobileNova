package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.api.fixNullColor
import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.core.domain.EpicsFilter
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.domain.RolesFilter
import com.grappim.taigamobile.core.domain.StatusesFilter
import com.grappim.taigamobile.core.domain.TagsFilter
import com.grappim.taigamobile.core.domain.UsersFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FiltersMapper @Inject constructor(
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {

    suspend fun toFiltersData(response: FiltersDataResponse): FiltersData =
        withContext(dispatcher) {
            FiltersData(
                assignees = response.assignedTo.map {
                    UsersFilter(
                        id = it.id,
                        name = it.fullName,
                        count = it.count
                    )
                },
                roles = response.roles.orEmpty().map {
                    RolesFilter(
                        id = it.id!!,
                        name = it.name!!,
                        count = it.count
                    )
                },
                tags = response.tags.orEmpty().map {
                    TagsFilter(
                        name = it.name!!,
                        color = it.color.fixNullColor(),
                        count = it.count
                    )
                },
                statuses = response.statuses.map {
                    StatusesFilter(
                        id = it.id!!,
                        color = it.color.fixNullColor(),
                        name = it.name!!,
                        count = it.count
                    )
                },
                createdBy = response.owners.map {
                    UsersFilter(
                        id = it.id!!,
                        name = it.fullName,
                        count = it.count
                    )
                },
                priorities = response.priorities.orEmpty().map {
                    StatusesFilter(
                        id = it.id!!,
                        color = it.color.fixNullColor(),
                        name = it.name!!,
                        count = it.count
                    )
                },
                severities = response.severities.orEmpty().map {
                    StatusesFilter(
                        id = it.id!!,
                        color = it.color.fixNullColor(),
                        name = it.name!!,
                        count = it.count
                    )
                },
                types = response.types.orEmpty().map {
                    StatusesFilter(
                        id = it.id!!,
                        color = it.color.fixNullColor(),
                        name = it.name!!,
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
