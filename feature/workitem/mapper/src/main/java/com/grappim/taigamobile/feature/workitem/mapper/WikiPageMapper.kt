package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import com.grappim.taigamobile.feature.workitem.dto.WikiPageDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WikiPageMapper @Inject constructor(@DefaultDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toDomain(dto: WikiPageDTO): WikiPage = withContext(dispatcher) {
        WikiPage(
            id = dto.id,
            version = dto.version,
            content = dto.content,
            editions = dto.editions,
            createdDate = dto.cratedDate,
            isWatcher = dto.isWatcher,
            lastModifier = dto.lastModifier,
            modifiedDate = dto.modifiedDate,
            totalWatchers = dto.totalWatchers,
            slug = dto.slug
        )
    }

    suspend fun toDomainList(dtos: List<WikiPageDTO>): ImmutableList<WikiPage> = withContext(dispatcher) {
        dtos.map { toDomain(it) }.toImmutableList()
    }
}
