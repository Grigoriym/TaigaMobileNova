package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.EpicShortInfoDTO
import com.grappim.taigamobile.core.domain.UserStoryShortInfoDTO
import com.grappim.taigamobile.feature.workitem.domain.EpicShortInfo
import com.grappim.taigamobile.feature.workitem.domain.UserStoryShortInfo
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserStoryShortInfoMapper @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toDomain(dto: UserStoryShortInfoDTO): UserStoryShortInfo = withContext(dispatcher) {
        UserStoryShortInfo(
            id = dto.id,
            ref = dto.ref,
            title = dto.title,
            epics = dto.epics.orEmpty().map { epicDto ->
                epicDto.toDomain()
            }.toImmutableList()
        )
    }

    private fun EpicShortInfoDTO.toDomain(): EpicShortInfo = EpicShortInfo(
        id = id,
        title = title,
        ref = ref,
        color = color
    )
}
