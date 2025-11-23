package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.core.domain.DueDateStatusDTO
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PatchedDataMapper @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) {
    suspend fun toDomain(resp: CommonTaskResponse): PatchedData = withContext(dispatcher) {
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
}
