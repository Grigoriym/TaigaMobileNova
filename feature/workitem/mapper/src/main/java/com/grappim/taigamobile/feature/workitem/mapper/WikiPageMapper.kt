package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class WikiPageMapper @Inject constructor() {

    fun toDomain(dto: WikiPageDTO): WikiPage = WikiPage(
        id = dto.id,
        version = dto.version,
        content = dto.content,
        editions = dto.editions,
        createdDate = dto.createdDate,
        isWatcher = dto.isWatcher,
        lastModifier = dto.lastModifierId,
        modifiedDate = dto.modifiedDate,
        totalWatchers = dto.totalWatchers,
        slug = dto.slug
    )

    fun toDomainList(dtos: List<WikiPageDTO>): ImmutableList<WikiPage> = dtos.map { toDomain(it) }.toImmutableList()
}
