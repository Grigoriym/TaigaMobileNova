package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiLinkDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WikiLinkMapper @Inject constructor(@DefaultDispatcher private val dispatcher: CoroutineDispatcher) {
    suspend fun toDomain(dto: WikiLinkDTO): WikiLink = withContext(dispatcher) {
        WikiLink(
            id = dto.id,
            order = dto.order,
            ref = dto.href,
            title = dto.title
        )
    }

    suspend fun toDomainList(dtos: List<WikiLinkDTO>): ImmutableList<WikiLink> =
        dtos.map { toDomain(it) }.toImmutableList()
}
