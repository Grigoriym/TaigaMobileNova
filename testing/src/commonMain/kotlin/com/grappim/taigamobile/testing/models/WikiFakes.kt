package com.grappim.taigamobile.testing.models

import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiLinkDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import com.grappim.taigamobile.testing.utils.getRandomBoolean
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDateTime

fun getWikiPageDTO(
    id: Long = getRandomLong(),
    projectId: Long = getRandomLong(),
    slug: String = getRandomString(),
    content: String = getRandomString(),
    lastModifierId: Long? = getRandomLong(),
    isWatcher: Boolean = getRandomBoolean(),
    version: Long = getRandomLong()
): WikiPageDTO = WikiPageDTO(
    id = id,
    projectId = projectId,
    slug = slug,
    content = content,
    ownerId = getRandomLong(),
    lastModifierId = lastModifierId,
    createdDate = nowLocalDateTime,
    modifiedDate = nowLocalDateTime,
    html = getRandomString(),
    editions = getRandomLong(),
    version = version,
    isWatcher = isWatcher,
    totalWatchers = getRandomLong()
)

fun getWikiLinkDTO(
    id: Long = getRandomLong(),
    projectId: Long = getRandomLong(),
    title: String = getRandomString()
): WikiLinkDTO = WikiLinkDTO(
    id = id,
    projectId = projectId,
    title = title,
    href = getRandomString(),
    order = getRandomLong()
)
