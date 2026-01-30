package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.domain.PatchedCustomAttributes
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import javax.inject.Inject

class PatchedDataMapper @Inject constructor(private val dueDateStatusMapper: DueDateStatusMapper) {

    fun toDomain(resp: WorkItemResponseDTO): PatchedData = PatchedData(
        newVersion = resp.version,
        dueDateStatus = dueDateStatusMapper.toDomain(resp.dueDateStatusDTO)
    )

    fun toDomainCustomAttrs(resp: CustomAttributesValuesResponseDTO): PatchedCustomAttributes =
        PatchedCustomAttributes(version = resp.version)

    fun fromWiki(dto: WikiPageDTO): PatchedData = PatchedData(
        newVersion = dto.version,
        dueDateStatus = null
    )
}
