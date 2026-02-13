package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.dto.AttachmentDTO

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
