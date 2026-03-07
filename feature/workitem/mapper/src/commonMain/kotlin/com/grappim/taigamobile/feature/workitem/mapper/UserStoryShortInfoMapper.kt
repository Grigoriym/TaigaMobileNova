package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.epics.dto.EpicShortInfoDTO
import com.grappim.taigamobile.feature.userstories.dto.UserStoryShortInfoDTO
import com.grappim.taigamobile.feature.workitem.domain.EpicShortInfo
import com.grappim.taigamobile.feature.workitem.domain.UserStoryShortInfo
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Factory

@Factory
class UserStoryShortInfoMapper {

    fun toDomain(dto: UserStoryShortInfoDTO): UserStoryShortInfo = UserStoryShortInfo(
        id = dto.id,
        ref = dto.ref,
        title = dto.title,
        epics = dto.epics.orEmpty().map { epicDto ->
            epicDto.toDomain()
        }.toImmutableList()
    )

    private fun EpicShortInfoDTO.toDomain(): EpicShortInfo = EpicShortInfo(
        id = id,
        title = title,
        ref = ref,
        color = color
    )
}
