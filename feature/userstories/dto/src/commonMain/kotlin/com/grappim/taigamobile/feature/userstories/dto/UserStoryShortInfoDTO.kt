package com.grappim.taigamobile.feature.userstories.dto

import com.grappim.taigamobile.feature.epics.dto.EpicShortInfoDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserStoryShortInfoDTO(
    val id: Long,
    val ref: Long,
    @SerialName(value = "subject")
    val title: String,
    val epics: List<EpicShortInfoDTO>?
) {
    val epicColors get() = epics?.map { it.color }.orEmpty()
}
