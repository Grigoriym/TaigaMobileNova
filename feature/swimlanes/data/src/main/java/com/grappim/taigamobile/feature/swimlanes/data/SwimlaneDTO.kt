package com.grappim.taigamobile.feature.swimlanes.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SwimlaneDTO(val id: Long, val name: String, val order: Long)
