package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import javax.inject.Inject

class DueDateStatusMapper @Inject constructor() {
    fun toDomain(dto: DueDateStatusDTO?): DueDateStatus? = when (dto) {
        DueDateStatusDTO.Set -> DueDateStatus.Set
        DueDateStatusDTO.DueSoon -> DueDateStatus.DueSoon
        DueDateStatusDTO.PastDue -> DueDateStatus.PastDue
        DueDateStatusDTO.NoLongerApplicable -> DueDateStatus.NoLongerApplicable
        DueDateStatusDTO.NotSet -> DueDateStatus.NotSet
        else -> null
    }
}
