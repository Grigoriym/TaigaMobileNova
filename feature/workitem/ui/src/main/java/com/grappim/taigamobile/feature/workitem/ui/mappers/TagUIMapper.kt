package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.filters.domain.model.filters.TagFilters
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.utils.ui.StaticStringColor
import com.grappim.taigamobile.utils.ui.toColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TagUIMapper @Inject constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) {

    suspend fun toSelectableUI(tag: Tag): SelectableTagUI = withContext(dispatcher) {
        SelectableTagUI(
            name = tag.name,
            color = tag.color.toColor()
        )
    }

    suspend fun toSelectableUI(list: ImmutableList<Tag>): ImmutableList<SelectableTagUI> = withContext(dispatcher) {
        list.map { toSelectableUI(it) }.toImmutableList()
    }

    suspend fun toUIFromFilters(tag: TagFilters): SelectableTagUI = withContext(dispatcher) {
        SelectableTagUI(
            name = tag.name,
            color = tag.color.toColor()
        )
    }

    suspend fun toUIFromFilters(list: ImmutableList<TagFilters>): ImmutableList<SelectableTagUI> =
        withContext(dispatcher) {
            list.map { toUIFromFilters(it) }.toImmutableList()
        }

    fun toTagUI(tag: Tag): TagUI = TagUI(
        name = tag.name,
        color = StaticStringColor(tag.color)
    )

    fun toTagUIList(list: ImmutableList<Tag>): ImmutableList<TagUI> = list.map { toTagUI(it) }.toImmutableList()
}
