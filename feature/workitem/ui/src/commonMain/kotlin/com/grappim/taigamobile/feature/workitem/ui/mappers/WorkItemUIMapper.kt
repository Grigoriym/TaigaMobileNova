package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemUI
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.koin.core.annotation.Factory

@Factory
class WorkItemUIMapper(
    private val statusUIMapper: StatusUIMapper,
    private val tagUIMapper: TagUIMapper,
    private val dateTimeUtils: DateTimeUtils
) {

    fun toUI(workItem: WorkItem): WorkItemUI = WorkItemUI(
        id = workItem.id,
        taskType = workItem.taskType,
        createdDate = dateTimeUtils.formatToMediumFormat(workItem.createdDate),
        status = statusUIMapper.toUI(workItem.status),
        ref = workItem.ref,
        title = workItem.title,
        isBlocked = workItem.isBlocked,
        tags = tagUIMapper.toSelectableUI(workItem.tags),
        isClosed = workItem.isClosed,
        colors = workItem.colors,
        assignee = workItem.assignee
    )


    fun toUI(list: ImmutableList<WorkItem>): ImmutableList<WorkItemUI> = list.map {
        toUI(it)
    }.toPersistentList()
}
