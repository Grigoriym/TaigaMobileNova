package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.EpicsFilter
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.RolesFilter
import com.grappim.taigamobile.core.domain.StatusesFilter
import com.grappim.taigamobile.core.domain.TagsFilter
import com.grappim.taigamobile.core.domain.UsersFilter
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

fun getFiltersData(
    newStatuses: ImmutableList<Statuses> = persistentListOf()
): FiltersData = FiltersData(
    statuses = newStatuses,
    types = persistentListOf(),
    severities = persistentListOf(),
    priorities = persistentListOf(),
    assignedTo = persistentListOf(),
    owners = persistentListOf(),
    tags = persistentListOf(),
    roles = persistentListOf()
)

fun getFiltersDataDTO(): FiltersDataDTO = FiltersDataDTO(
    query = getRandomString(),
    assignees = listOf(
        getUsersFilter(),
        getUsersFilter()
    ),
    roles = listOf(
        getRolesFilter(),
        getRolesFilter()
    ),
    tags = listOf(
        getTagsFilter(),
        getTagsFilter()
    ),
    statuses = listOf(
        getStatusesFilter(),
        getStatusesFilter()
    ),
    createdBy = listOf(
        getUsersFilter(),
        getUsersFilter()
    ),
    epics = listOf(
        getEpicsFilter(),
        getEpicsFilter()
    ),
    priorities = listOf(
        getStatusesFilter(),
        getStatusesFilter()
    ),
    severities = listOf(
        getStatusesFilter(),
        getStatusesFilter()
    ),
    types = listOf(
        getStatusesFilter(),
        getStatusesFilter()
    )
)

fun getTagsFilter(): TagsFilter = TagsFilter(
    name = getRandomString(),
    color = getRandomString(),
    count = getRandomLong()
)

fun getEpicsFilter(): EpicsFilter = EpicsFilter(
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong()
)

fun getUsersFilter(): UsersFilter = UsersFilter(
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong()
)

fun getRolesFilter(): RolesFilter = RolesFilter(
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong()
)

fun getStatusesFilter(): StatusesFilter = StatusesFilter(
    id = getRandomLong(),
    color = getRandomString(),
    name = getRandomString(),
    count = getRandomLong()
)
