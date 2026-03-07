package com.grappim.taigamobile.feature.filters.dto

import kotlinx.serialization.Serializable

@Serializable
data class TagsFilterDTO(override val name: String, override val color: String, override val count: Long) : FilterDTO {
    override val id: Long? = null
}
