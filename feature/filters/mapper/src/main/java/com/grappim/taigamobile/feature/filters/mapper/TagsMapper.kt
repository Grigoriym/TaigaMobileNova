package com.grappim.taigamobile.feature.filters.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.workitem.dto.TagWithColor
import com.grappim.taigamobile.utils.ui.fixNullColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TagsMapper @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher) {
    suspend fun toTags(tags: List<TagWithColor>?): ImmutableList<Tag> = withContext(ioDispatcher) {
        tags.orEmpty()
            .map { tag ->
                Tag(
                    name = tag.getOrNull(0).orEmpty(),
                    color = tag.getOrNull(1).fixNullColor()
                )
            }.toImmutableList()
    }

    fun toTags(response: Map<String, String?>): ImmutableList<Tag> = response.map { (name, color) ->
        Tag(name = name, color = color.fixNullColor())
    }.toImmutableList()
}
