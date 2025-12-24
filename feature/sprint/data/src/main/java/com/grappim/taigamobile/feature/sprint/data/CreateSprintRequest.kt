package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.core.serialization.LocalDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class CreateSprintRequest(
    val name: String,
    @SerialName(value = "estimated_start")
    @Serializable(with = LocalDateSerializer::class)
    val estimatedStart: LocalDate,
    @SerialName(value = "estimated_finish")
    @Serializable(with = LocalDateSerializer::class)
    val estimatedFinish: LocalDate,
    val project: Long
)
