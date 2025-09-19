package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.workitem.data.WorkItemResponseDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatusMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    @Deprecated("use another one")
    suspend fun getStatus(filtersData: FiltersData, resp: CommonTaskResponse): Status? =
        withContext(ioDispatcher) {
            val currentStatus = filtersData.statuses.find {
                resp.status == it.id
            } ?: return@withContext null
            Status(
                id = resp.status,
                name = resp.statusExtraInfo.name,
                color = resp.statusExtraInfo.color,
                count = currentStatus.count,
                order = currentStatus.order
            )
        }

    suspend fun getStatus(filtersData: FiltersData, resp: WorkItemResponseDTO): Status? =
        withContext(ioDispatcher) {
            val currentStatus = filtersData.statuses.find {
                resp.status == it.id
            } ?: return@withContext null
            Status(
                id = resp.status,
                name = resp.statusExtraInfo.name,
                color = resp.statusExtraInfo.color,
                count = currentStatus.count,
                order = currentStatus.order
            )
        }
}
