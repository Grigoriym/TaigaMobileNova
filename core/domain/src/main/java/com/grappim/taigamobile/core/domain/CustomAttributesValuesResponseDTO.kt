package com.grappim.taigamobile.core.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomAttributesValuesResponseDTO(
    @Json(name = "attributes_values")
    val attributesValues: Map<Long, Any?>,
    val version: Long
)
