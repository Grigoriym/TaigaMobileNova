package com.grappim.taigamobile.feature.filters.domain.model.filters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RoleFilters")
data class RoleFilters(
    override val color: String,
    override val count: Long,
    override val id: Long,
    override val name: String
) : Filters
