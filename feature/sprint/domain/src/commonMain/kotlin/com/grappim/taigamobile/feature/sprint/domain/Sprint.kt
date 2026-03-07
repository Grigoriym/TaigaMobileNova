package com.grappim.taigamobile.feature.sprint.domain

import kotlinx.datetime.LocalDate

data class Sprint(
    val id: Long,
    val name: String,
    val order: Int,
    val start: LocalDate,
    val end: LocalDate,
    val storiesCount: Int,
    val isClosed: Boolean
)
