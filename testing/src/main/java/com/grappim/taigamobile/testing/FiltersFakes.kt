package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.filters.StatusFilters
import com.grappim.taigamobile.feature.filters.domain.model.filters.TagFilters
import com.grappim.taigamobile.feature.filters.dto.EpicsFilterDTO
import com.grappim.taigamobile.feature.filters.dto.FiltersDataDTO
import com.grappim.taigamobile.feature.filters.dto.RolesFilterDTO
import com.grappim.taigamobile.feature.filters.dto.StatusesFilterDTO
import com.grappim.taigamobile.feature.filters.dto.TagsFilterDTO
import com.grappim.taigamobile.feature.filters.dto.UsersFilterDTO
import kotlinx.collections.immutable.persistentListOf

fun getFiltersData(): FiltersData = FiltersData(
    statuses = persistentListOf(),
    types = persistentListOf(),
    severities = persistentListOf(),
    priorities = persistentListOf(),
    tags = persistentListOf(),
    roles = persistentListOf(),
    epics = persistentListOf(),
    assignees = persistentListOf(),
    createdBy = persistentListOf()
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
        getTagsFilterDTO(),
        getTagsFilterDTO()
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

fun getTagsFilterDTO(): TagsFilterDTO = TagsFilterDTO(
    name = getRandomString(),
    color = getRandomString(),
    count = getRandomLong()
)

fun getTagFilters(
    color: String = getRandomString()
): TagFilters = TagFilters(
    name = getRandomString(),
    color = color,
    count = getRandomLong()
)

fun getEpicsFilter(): EpicsFilterDTO = EpicsFilterDTO(
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong()
)

fun getUsersFilter(): UsersFilterDTO = UsersFilterDTO(
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong()
)

fun getRolesFilter(): RolesFilterDTO = RolesFilterDTO(
    id = getRandomLong(),
    name = getRandomString(),
    count = getRandomLong()
)

fun getStatusesFilter(): StatusesFilterDTO = StatusesFilterDTO(
    id = getRandomLong(),
    color = getRandomString(),
    name = getRandomString(),
    count = getRandomLong()
)

fun getStatusFilters(
    id: Long = getRandomLong(),
    name: String = getRandomString(),
    color: String = getRandomString()
): StatusFilters = StatusFilters(
    id = id,
    name = name,
    count = getRandomLong(),
    color = color
)
