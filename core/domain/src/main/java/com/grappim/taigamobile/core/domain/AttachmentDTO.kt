package com.grappim.taigamobile.core.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttachmentDTO(val id: Long, val name: String, @Json(name = "size") val sizeInBytes: Long, val url: String)
