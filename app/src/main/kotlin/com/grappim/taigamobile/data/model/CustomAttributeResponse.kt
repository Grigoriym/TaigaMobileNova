package com.grappim.taigamobile.data.model

import com.grappim.taigamobile.core.domain.CustomFieldType
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomAttributeResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val order: Int,
    val type: CustomFieldType,
    val extra: List<String>?
)
