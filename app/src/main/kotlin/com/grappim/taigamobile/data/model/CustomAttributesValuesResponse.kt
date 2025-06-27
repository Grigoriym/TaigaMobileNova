package com.grappim.taigamobile.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomAttributesValuesResponse(
    @Json(name = "attributes_values")
    val attributesValues: Map<Long, Any?>,
    val version: Int
)
