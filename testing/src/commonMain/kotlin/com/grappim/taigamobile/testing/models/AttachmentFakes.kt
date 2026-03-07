package com.grappim.taigamobile.testing.models

import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.dto.AttachmentDTO
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString

fun getAttachment(): Attachment = Attachment(
    id = getRandomLong(),
    name = getRandomString(),
    sizeInBytes = getRandomLong(),
    url = getRandomString()
)

fun getAttachmentDTO(): AttachmentDTO = AttachmentDTO(
    id = getRandomLong(),
    name = getRandomString(),
    sizeInBytes = getRandomLong(),
    url = getRandomString()
)
