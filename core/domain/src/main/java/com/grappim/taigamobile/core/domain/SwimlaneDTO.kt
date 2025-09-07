package com.grappim.taigamobile.core.domain

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SwimlaneDTO(val id: Long, val name: String, val order: Long)
