package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.dto.AttachmentDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class AttachmentMapper @Inject constructor() {
    fun toDomain(dto: AttachmentDTO): Attachment = Attachment(
        id = dto.id,
        name = dto.name,
        sizeInBytes = dto.sizeInBytes,
        url = dto.url
    )

    fun toDomain(list: List<AttachmentDTO>): ImmutableList<Attachment> = list.map { dto ->
        toDomain(dto)
    }.toImmutableList()
}
