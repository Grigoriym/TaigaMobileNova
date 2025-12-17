package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.domain.Attachment

fun getAttachment(): Attachment = Attachment(
    id = getRandomLong(),
    name = getRandomString(),
    sizeInBytes = getRandomLong(),
    url = getRandomString()
)
