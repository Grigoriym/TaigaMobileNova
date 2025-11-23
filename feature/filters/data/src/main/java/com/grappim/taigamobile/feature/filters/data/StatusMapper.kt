package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.workitem.data.WorkItemResponseDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatusMapper @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    suspend fun getStatus(resp: WorkItemResponseDTO): Status = withContext(ioDispatcher) {
        Status(
            id = resp.status,
            name = resp.statusExtraInfo.name,
            color = resp.statusExtraInfo.color
        )
    }
}
