package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DueDateStatusMapper @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher) {
    suspend fun toDomain(dto: DueDateStatusDTO?): DueDateStatus? = withContext(ioDispatcher) {
        when (dto) {
            DueDateStatusDTO.Set -> DueDateStatus.Set
            DueDateStatusDTO.DueSoon -> DueDateStatus.DueSoon
            DueDateStatusDTO.PastDue -> DueDateStatus.PastDue
            DueDateStatusDTO.NoLongerApplicable -> DueDateStatus.NoLongerApplicable
            DueDateStatusDTO.NotSet -> DueDateStatus.NotSet
            else -> null
        }
    }
}
