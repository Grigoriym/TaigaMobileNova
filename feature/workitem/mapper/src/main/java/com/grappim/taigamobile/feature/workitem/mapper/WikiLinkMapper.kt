package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiLinkDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class WikiLinkMapper @Inject constructor() {
    fun toDomain(dto: WikiLinkDTO): WikiLink = WikiLink(
        id = dto.id,
        order = dto.order,
        ref = dto.href,
        title = dto.title
    )

    fun toDomainList(dtos: List<WikiLinkDTO>): ImmutableList<WikiLink> = dtos.map { toDomain(it) }.toImmutableList()
}
