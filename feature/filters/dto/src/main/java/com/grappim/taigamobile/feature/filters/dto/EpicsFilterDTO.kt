package com.grappim.taigamobile.feature.filters.dto

import kotlinx.serialization.Serializable

@Serializable
data class EpicsFilterDTO(override val id: Long?, override val name: String, override val count: Long) : FilterDTO {
    override val color: String? = null
}
