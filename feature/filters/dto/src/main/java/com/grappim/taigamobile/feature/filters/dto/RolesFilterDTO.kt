package com.grappim.taigamobile.feature.filters.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RolesFilterDTO(override val id: Long, override val name: String, override val count: Long) : FilterDTO {
    override val color: String? = null
}
