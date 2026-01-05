package com.grappim.taigamobile.feature.filters.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.filters.domain.model.Priority
import com.grappim.taigamobile.feature.filters.domain.model.Severity
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Type
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import com.grappim.taigamobile.utils.ui.fixNullColor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatusesMapper @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    suspend fun getStatus(resp: WorkItemResponseDTO): Status = withContext(ioDispatcher) {
        Status(
            id = resp.status,
            name = resp.statusExtraInfo.name,
            color = resp.statusExtraInfo.color
        )
    }

    fun getType(filtersData: FiltersData, resp: WorkItemResponseDTO): Type? {
        val typeId = resp.type ?: return null
        val currentItem = filtersData.types.find { typeId == it.id } ?: return null
        return Type(
            id = typeId,
            name = currentItem.name,
            color = currentItem.color.fixNullColor()
        )
    }

    fun getSeverity(filtersData: FiltersData, resp: WorkItemResponseDTO): Severity? {
        val severityId = resp.severity ?: return null
        val currentItem = filtersData.severities.find { severityId == it.id } ?: return null
        return Severity(
            id = severityId,
            name = currentItem.name,
            color = currentItem.color.fixNullColor()
        )
    }

    fun getPriority(filtersData: FiltersData, resp: WorkItemResponseDTO): Priority? {
        val priorityId = resp.priority ?: return null
        val currentItem = filtersData.priorities.find { priorityId == it.id } ?: return null
        return Priority(
            id = priorityId,
            name = currentItem.name,
            color = currentItem.color.fixNullColor()
        )
    }
}
