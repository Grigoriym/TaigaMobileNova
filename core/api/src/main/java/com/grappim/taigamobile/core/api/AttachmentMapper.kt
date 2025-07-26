package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.AttachmentDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AttachmentMapper @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend fun toDomain(dto: AttachmentDTO): Attachment = withContext(dispatcher) {
        Attachment(
            id = dto.id,
            name = dto.name,
            sizeInBytes = dto.sizeInBytes,
            url = dto.url
        )
    }
}
