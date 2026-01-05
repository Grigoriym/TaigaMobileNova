package com.grappim.taigamobile.feature.workitem.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CustomAttributesValuesResponseDTO(
    @SerialName(value = "attributes_values")
    val attributesValues: Map<String, JsonElement>,
    val version: Long
)
