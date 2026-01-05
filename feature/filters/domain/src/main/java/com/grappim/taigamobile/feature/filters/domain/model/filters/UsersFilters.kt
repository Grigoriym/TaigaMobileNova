package com.grappim.taigamobile.feature.filters.domain.model.filters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("UsersFilters")
data class UsersFilters(
    override val id: Long?,
    override val name: String,
    override val count: Long,
    override val color: String? = null
) : Filters
