package com.grappim.taigamobile.feature.sprint.data

import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class CreateSprintRequest(
    val name: String,
    val estimated_start: LocalDate,
    val estimated_finish: LocalDate,
    val project: Long
)
