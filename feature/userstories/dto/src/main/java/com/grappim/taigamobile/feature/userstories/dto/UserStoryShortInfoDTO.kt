package com.grappim.taigamobile.feature.userstories.dto

import com.grappim.taigamobile.feature.epics.dto.EpicShortInfoDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserStoryShortInfoDTO(
    val id: Long,
    val ref: Long,
    @Json(name = "subject") val title: String,
    val epics: List<EpicShortInfoDTO>?
) {
    val epicColors get() = epics?.map { it.color }.orEmpty()
}
