package com.grappim.taigamobile.feature.filters.domain.model

import kotlinx.collections.immutable.ImmutableList

data class FiltersData(
    val assignedTo: ImmutableList<AssignedTo>,
    val owners: ImmutableList<Owner>,
    val roles: ImmutableList<Role>,
    val tags: ImmutableList<Tag>,
    val severities: ImmutableList<Statuses>,
    val statuses: ImmutableList<Statuses>,
    val priorities: ImmutableList<Statuses>,
    val types: ImmutableList<Statuses>
)
