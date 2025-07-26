package com.grappim.taigamobile.core.domain

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomAttributeResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val order: Long,
    val type: CustomFieldType,
    val extra: List<String>?
)
