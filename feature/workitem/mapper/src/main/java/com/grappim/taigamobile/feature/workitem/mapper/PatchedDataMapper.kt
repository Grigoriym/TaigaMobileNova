package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.workitem.domain.PatchedCustomAttributes
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PatchedDataMapper @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val dueDateStatusMapper: DueDateStatusMapper
) {

    suspend fun toDomain(resp: WorkItemResponseDTO): PatchedData = withContext(dispatcher) {
        PatchedData(
            newVersion = resp.version,
            dueDateStatus = dueDateStatusMapper.toDomain(resp.dueDateStatusDTO)
        )
    }

    suspend fun toDomainCustomAttrs(resp: CustomAttributesValuesResponseDTO): PatchedCustomAttributes =
        withContext(dispatcher) {
            PatchedCustomAttributes(version = resp.version)
        }

    suspend fun fromWiki(dto: WikiPageDTO): PatchedData = withContext(dispatcher) {
        PatchedData(
            newVersion = dto.version,
            dueDateStatus = null
        )
    }
}
