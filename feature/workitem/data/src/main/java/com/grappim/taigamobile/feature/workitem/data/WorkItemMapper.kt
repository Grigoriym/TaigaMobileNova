package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.workitem.domain.UpdateWorkItem
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WorkItemMapper @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toUpdateDomain(dto: WorkItemResponseDTO): UpdateWorkItem = withContext(dispatcher) {
        UpdateWorkItem(
            watcherUserIds = dto.watchers.orEmpty().toImmutableList()
        )
    }
}
