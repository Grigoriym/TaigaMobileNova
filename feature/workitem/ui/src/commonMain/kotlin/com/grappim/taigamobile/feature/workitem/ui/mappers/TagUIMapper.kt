package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.filters.domain.model.TagFilters
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.utils.ui.StaticStringColor
import com.grappim.taigamobile.utils.ui.toColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Factory

@Factory
class TagUIMapper {

    fun toSelectableUI(tag: Tag): SelectableTagUI = SelectableTagUI(
        name = tag.name,
        color = tag.color.toColor()
    )

    fun toSelectableUI(list: ImmutableList<Tag>): ImmutableList<SelectableTagUI> =
        list.map { toSelectableUI(it) }.toImmutableList()

    fun toUIFromFilters(tag: TagFilters): SelectableTagUI = SelectableTagUI(
        name = tag.name,
        color = tag.color.toColor()
    )

    fun toUIFromFilters(list: ImmutableList<TagFilters>): ImmutableList<SelectableTagUI> =
        list.map { toUIFromFilters(it) }.toImmutableList()

    fun toTagUI(tag: Tag): TagUI = TagUI(
        name = tag.name,
        color = StaticStringColor(tag.color)
    )

    fun toTagUIList(list: ImmutableList<Tag>): ImmutableList<TagUI> = list.map { toTagUI(it) }.toImmutableList()
}
