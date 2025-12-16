package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.domain.PatchedCustomAttributes
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.dto.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import com.grappim.taigamobile.feature.workitem.dto.WikiPageDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PatchedDataMapper @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toDomain(resp: WorkItemResponseDTO): PatchedData = withContext(dispatcher) {
        PatchedData(
            newVersion = resp.version,
            dueDateStatus = when (resp.dueDateStatusDTO) {
                DueDateStatusDTO.Set -> DueDateStatus.Set
                DueDateStatusDTO.DueSoon -> DueDateStatus.DueSoon
                DueDateStatusDTO.PastDue -> DueDateStatus.PastDue
                DueDateStatusDTO.NoLongerApplicable -> DueDateStatus.NoLongerApplicable
                DueDateStatusDTO.NotSet -> DueDateStatus.NotSet
                else -> null
            }
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
