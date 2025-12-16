package com.grappim.taigamobile.feature.filters.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusesFilterDTO(
    override val id: Long,
    override val color: String,
    override val name: String,
    override val count: Long
) : FilterDTO
