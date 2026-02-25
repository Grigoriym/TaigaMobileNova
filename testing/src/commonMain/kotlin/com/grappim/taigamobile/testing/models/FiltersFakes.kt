package com.grappim.taigamobile.testing.models

import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.StatusFilters
import com.grappim.taigamobile.feature.filters.domain.model.TagFilters
import com.grappim.taigamobile.feature.filters.dto.EpicsFilterDTO
import com.grappim.taigamobile.feature.filters.dto.FiltersDataDTO
import com.grappim.taigamobile.feature.filters.dto.FiltersDataResponseDTO
import com.grappim.taigamobile.feature.filters.dto.RolesFilterDTO
import com.grappim.taigamobile.feature.filters.dto.StatusesFilterDTO
import com.grappim.taigamobile.feature.filters.dto.TagsFilterDTO
import com.grappim.taigamobile.feature.filters.dto.UsersFilterDTO
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
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

fun getFiltersDataResponseDTO(
    statuses: List<FiltersDataResponseDTO.Filter> = listOf(
        getFiltersResponseFilter(),
        getFiltersResponseFilter()
    )
): FiltersDataResponseDTO = FiltersDataResponseDTO(
    statuses = statuses,
    tags = null,
    roles = null,
    assignedTo = emptyList(),
    owners = emptyList(),
    epics = null,
    priorities = null,
    severities = null,
    types = null
)

fun getFiltersResponseFilter(): FiltersDataResponseDTO.Filter = FiltersDataResponseDTO.Filter(
    id = getRandomLong(),
    name = getRandomString(),
    color = getRandomString(),
    count = getRandomLong(),
    order = getRandomLong()
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
