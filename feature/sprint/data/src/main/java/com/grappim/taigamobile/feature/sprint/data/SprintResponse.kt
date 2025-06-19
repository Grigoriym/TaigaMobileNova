package com.grappim.taigamobile.feature.sprint.data

import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class SprintResponse(
    val id: Long,
    val name: String,
    val estimated_start: LocalDate,
    val estimated_finish: LocalDate,
    val closed: Boolean,
    val order: Int,
    val user_stories: List<UserStory>
) {
    @JsonClass(generateAdapter = true)
    data class UserStory(
        val id: Long
    )
}
