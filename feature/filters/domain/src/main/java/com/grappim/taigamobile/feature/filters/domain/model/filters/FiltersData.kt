package com.grappim.taigamobile.feature.filters.domain.model.filters

import com.grappim.taigamobile.core.serialization.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable

@Serializable
data class FiltersData(
    @Serializable(with = ImmutableListSerializer::class)
    val roles: ImmutableList<RoleFilters> = persistentListOf(),
    @Serializable(with = ImmutableListSerializer::class)
    val tags: ImmutableList<TagFilters> = persistentListOf(),
    @Serializable(with = ImmutableListSerializer::class)
    val epics: ImmutableList<EpicsFilters> = persistentListOf(),

    @Serializable(with = ImmutableListSerializer::class)
    val severities: ImmutableList<StatusFilters> = persistentListOf(),
    @Serializable(with = ImmutableListSerializer::class)
    val statuses: ImmutableList<StatusFilters> = persistentListOf(),
    @Serializable(with = ImmutableListSerializer::class)
    val priorities: ImmutableList<StatusFilters> = persistentListOf(),
    @Serializable(with = ImmutableListSerializer::class)
    val types: ImmutableList<StatusFilters> = persistentListOf(),

    @Serializable(with = ImmutableListSerializer::class)
    val assignees: ImmutableList<UsersFilters> = persistentListOf(),
    @Serializable(with = ImmutableListSerializer::class)
    val createdBy: ImmutableList<UsersFilters> = persistentListOf()
) {
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

    operator fun minus(other: FiltersData) = FiltersData(
        assignees = (assignees - other.assignees.toSet()).toImmutableList(),
        roles = (roles - other.roles.toSet()).toImmutableList(),
        tags = (tags - other.tags.toSet()).toImmutableList(),
        statuses = (statuses - other.statuses.toSet()).toImmutableList(),
        createdBy = (createdBy - other.createdBy.toSet()).toImmutableList(),
        priorities = (priorities - other.priorities.toSet()).toImmutableList(),
        severities = (severities - other.severities.toSet()).toImmutableList(),
        types = (types - other.types.toSet()).toImmutableList(),
        epics = (epics - other.epics.toSet()).toImmutableList()
    )

    fun updateData(other: FiltersData): FiltersData {
        fun List<UsersFilters>.updateUsers(other: List<UsersFilters>) = map { current ->
            other.find { new -> current.id == new.id }?.let {
                current.copy(name = it.name, count = it.count)
            } ?: current.copy(count = 0)
        }

        fun List<StatusFilters>.updateStatuses(other: List<StatusFilters>) = map { current ->
            other.find { new -> current.id == new.id }?.let {
                current.copy(name = it.name, color = it.color, count = it.count)
            } ?: current.copy(count = 0)
        }

        return FiltersData(
            assignees = assignees.updateUsers(other.assignees).toImmutableList(),
            roles = roles.map { current ->
                other.roles.find { new -> current.id == new.id }?.let {
                    current.copy(name = it.name, count = it.count)
                } ?: current.copy(count = 0)
            }.toImmutableList(),
            tags = tags.map { current ->
                other.tags.find { new -> current.name == new.name }?.let {
                    current.copy(color = it.color, count = it.count)
                } ?: current.copy(count = 0)
            }.toImmutableList(),
            statuses = statuses.updateStatuses(other.statuses).toImmutableList(),
            createdBy = createdBy.updateUsers(other.createdBy).toImmutableList(),
            epics = epics.map { current ->
                other.epics.find { new -> current.id == new.id }?.let {
                    current.copy(name = it.name, count = it.count)
                } ?: current.copy(count = 0)
            }.toImmutableList(),
            priorities = priorities.updateStatuses(other.priorities).toImmutableList(),
            severities = severities.updateStatuses(other.severities).toImmutableList(),
            types = types.updateStatuses(other.types).toImmutableList()
        )
    }
}
