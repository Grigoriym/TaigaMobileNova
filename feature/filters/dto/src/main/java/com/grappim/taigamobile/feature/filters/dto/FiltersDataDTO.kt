package com.grappim.taigamobile.feature.filters.dto

import kotlinx.serialization.Serializable

@Serializable
data class FiltersDataDTO(
    val query: String = "",
    val assignees: List<UsersFilterDTO> = emptyList(),
    val roles: List<RolesFilterDTO> = emptyList(),
    val tags: List<TagsFilterDTO> = emptyList(),
    val statuses: List<StatusesFilterDTO> = emptyList(),
    val createdBy: List<UsersFilterDTO> = emptyList(),

    // user story filters
    val epics: List<EpicsFilterDTO> = emptyList(),

    // issue filters
    val priorities: List<StatusesFilterDTO> = emptyList(),
    val severities: List<StatusesFilterDTO> = emptyList(),
    val types: List<StatusesFilterDTO> = emptyList()
) {
    operator fun minus(other: FiltersDataDTO) = FiltersDataDTO(
        assignees = assignees - other.assignees.toSet(),
        roles = roles - other.roles.toSet(),
        tags = tags - other.tags.toSet(),
        statuses = statuses - other.statuses.toSet(),
        createdBy = createdBy - other.createdBy.toSet(),
        priorities = priorities - other.priorities.toSet(),
        severities = severities - other.severities.toSet(),
        types = types - other.types.toSet(),
        epics = epics - other.epics.toSet()
    )

    // updates current filters data using other filters data
    // (helpful for updating already selected filters)
    fun updateData(other: FiltersDataDTO): FiltersDataDTO {
        fun List<UsersFilterDTO>.updateUsers(other: List<UsersFilterDTO>) = map { current ->
            other.find { new -> current.id == new.id }?.let {
                current.copy(name = it.name, count = it.count)
            } ?: current.copy(count = 0)
        }

        fun List<StatusesFilterDTO>.updateStatuses(other: List<StatusesFilterDTO>) = map { current ->
            other.find { new -> current.id == new.id }?.let {
                current.copy(name = it.name, color = it.color, count = it.count)
            } ?: current.copy(count = 0)
        }

        return FiltersDataDTO(
            assignees = assignees.updateUsers(other.assignees),
            roles = roles.map { current ->
                other.roles.find { new -> current.id == new.id }?.let {
                    current.copy(name = it.name, count = it.count)
                } ?: current.copy(count = 0)
            },
            tags = tags.map { current ->
                other.tags.find { new -> current.name == new.name }?.let {
                    current.copy(color = it.color, count = it.count)
                } ?: current.copy(count = 0)
            },
            statuses = statuses.updateStatuses(other.statuses),
            createdBy = createdBy.updateUsers(other.createdBy),
            epics = epics.map { current ->
                other.epics.find { new -> current.id == new.id }?.let {
                    current.copy(name = it.name, count = it.count)
                } ?: current.copy(count = 0)
            },
            priorities = priorities.updateStatuses(other.priorities),
            severities = severities.updateStatuses(other.severities),
            types = types.updateStatuses(other.types)
        )
    }

    val filtersNumber = listOf(
        assignees,
        roles,
        tags,
        statuses,
        createdBy,
        priorities,
        severities,
        types,
        epics
    ).sumOf { it.size }
}

fun List<FilterDTO>.hasData() = any { it.count > 0 }

fun List<FilterDTO>.commaString() = map { it.id }
    .joinToString(separator = ",")
    .takeIf { it.isNotEmpty() }

fun List<TagsFilterDTO>.tagsCommaString() = joinToString(separator = ",") { it.name.replace(" ", "+") }
    .takeIf { it.isNotEmpty() }
