package com.grappim.taigamobile.feature.filters.dto

sealed interface FilterDTO {
    val id: Long?
    val name: String
    val count: Long
    val color: String?
}
