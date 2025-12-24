package com.grappim.taigamobile.feature.filters.dto

import kotlinx.serialization.Serializable

@Serializable
data class StatusesFilterDTO(
    override val id: Long,
    override val color: String,
    override val name: String,
    override val count: Long
) : FilterDTO
