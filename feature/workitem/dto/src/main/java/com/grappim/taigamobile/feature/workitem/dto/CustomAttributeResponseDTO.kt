package com.grappim.taigamobile.feature.workitem.dto

import com.grappim.taigamobile.feature.workitem.dto.customfield.CustomFieldTypeDTO
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomAttributeResponseDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val order: Long,
    val type: CustomFieldTypeDTO,
    val extra: List<String>?
)
