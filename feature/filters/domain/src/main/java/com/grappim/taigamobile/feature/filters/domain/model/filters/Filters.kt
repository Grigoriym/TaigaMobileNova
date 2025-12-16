package com.grappim.taigamobile.feature.filters.domain.model.filters

sealed interface Filters {
    val id: Long?
    val name: String
    val count: Long
    val color: String?
}
