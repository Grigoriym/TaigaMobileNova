package com.grappim.taigamobile.feature.filters.domain.model.filters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TagFilters")
data class TagFilters(
    override val id: Long? = null,
    override val name: String,
    override val count: Long,
    override val color: String
) : Filters
