package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemUI
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WorkItemUIMapper @Inject constructor(
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val statusUIMapper: StatusUIMapper,
    private val tagUIMapper: TagUIMapper,
    private val dateTimeUtils: DateTimeUtils
) {

    suspend fun toUI(workItem: WorkItem): WorkItemUI = withContext(dispatcher) {
        WorkItemUI(
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
    }

    suspend fun toUI(list: ImmutableList<WorkItem>): ImmutableList<WorkItemUI> = withContext(dispatcher) {
        list.map {
            toUI(it)
        }.toPersistentList()
    }
}
