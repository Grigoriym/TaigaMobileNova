package com.grappim.taigamobile.feature.epics.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EpicShortInfoDTO(val id: Long, @Json(name = "subject") val title: String, val ref: Long, val color: String)
