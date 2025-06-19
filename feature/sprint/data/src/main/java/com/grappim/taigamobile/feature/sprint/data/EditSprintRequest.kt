package com.grappim.taigamobile.feature.sprint.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class EditSprintRequest(
    val name: String,
    @Json(name = "estimated_start")
    val estimatedStart: LocalDate,
    @Json(name = "estimated_finish")
    val estimatedFinish: LocalDate
)
