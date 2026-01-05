package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.dto.AttachmentDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AttachmentMapper @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) {
    suspend fun toDomain(dto: AttachmentDTO): Attachment = withContext(dispatcher) {
        Attachment(
            id = dto.id,
            name = dto.name,
            sizeInBytes = dto.sizeInBytes,
            url = dto.url
        )
    }

    suspend fun toDomain(list: List<AttachmentDTO>): ImmutableList<Attachment> = withContext(dispatcher) {
        list.map { dto ->
            toDomain(dto)
        }.toImmutableList()
    }
}
