package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentMap
import javax.inject.Inject

class WorkItemRepositoryImpl @Inject constructor(
    private val workItemApi: WorkItemApi,
    private val patchedDataMapper: PatchedDataMapper
) : WorkItemRepository {

    override suspend fun patchData(
        version: Long,
        id: Long,
        payload: ImmutableMap<String, Any?>,
        taskPath: WorkItemPathPlural
    ): PatchedData {
        val editedMap = payload.toPersistentMap().put("version", version)
        val result = workItemApi.patchWorkItem(
            taskPath = taskPath,
            id = id,
            payload = editedMap
        )
        return patchedDataMapper.toDomain(result)
    }
}
