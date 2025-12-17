package com.grappim.taigamobile.feature.sprint.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class SprintResponseDTO(
    val id: Long,
    val name: String,
    @Json(name = "estimated_start")
    val estimatedStart: LocalDate,
    @Json(name = "estimated_finish")
    val estimatedFinish: LocalDate,
    val closed: Boolean,
    val order: Int,
    @Json(name = "user_stories")
    val userStories: List<SprintUserStoryDTO>
) {
    @JsonClass(generateAdapter = true)
    data class SprintUserStoryDTO(val id: Long)
}
