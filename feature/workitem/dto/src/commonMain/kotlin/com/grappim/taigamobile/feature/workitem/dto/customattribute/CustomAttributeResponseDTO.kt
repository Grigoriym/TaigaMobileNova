package com.grappim.taigamobile.feature.workitem.dto.customattribute

import com.grappim.taigamobile.feature.workitem.dto.customfield.CustomFieldTypeDTO
import kotlinx.serialization.Serializable

@Serializable
data class CustomAttributeResponseDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val order: Long,
    val type: CustomFieldTypeDTO,
    val extra: List<String>?
)
