package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.filters.domain.model.filters.TagFilters
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.utils.ui.toColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TagUIMapper @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toUI(tag: Tag): TagUI = withContext(dispatcher) {
        TagUI(
            name = tag.name,
            color = tag.color.toColor()
        )
    }

    suspend fun toUI(list: ImmutableList<Tag>): ImmutableList<TagUI> = withContext(dispatcher) {
        list.map { toUI(it) }.toImmutableList()
    }

    suspend fun toUIFromFilters(tag: TagFilters): TagUI = withContext(dispatcher) {
        TagUI(
            name = tag.name,
            color = tag.color.toColor()
        )
    }

    suspend fun toUIFromFilters(list: ImmutableList<TagFilters>): ImmutableList<TagUI> = withContext(dispatcher) {
        list.map { toUIFromFilters(it) }.toImmutableList()
    }
}
