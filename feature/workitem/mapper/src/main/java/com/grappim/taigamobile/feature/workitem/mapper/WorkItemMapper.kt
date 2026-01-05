package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.UpdateWorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WorkItemMapper @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val statusesMapper: StatusesMapper,
    private val userMapper: UserMapper,
    private val tagsMapper: TagsMapper,
    private val projectMapper: ProjectMapper
) {

    suspend fun toUpdateDomain(dto: WorkItemResponseDTO): UpdateWorkItem = withContext(dispatcher) {
        UpdateWorkItem(
            watcherUserIds = dto.watchers.orEmpty().toImmutableList()
        )
    }

    suspend fun toDomainList(dtos: List<WorkItemResponseDTO>, taskType: CommonTaskType): ImmutableList<WorkItem> =
        dtos.map { toDomain(it, taskType) }.toImmutableList()

    suspend fun toDomain(dto: WorkItemResponseDTO, taskType: CommonTaskType): WorkItem = withContext(dispatcher) {
        WorkItem(
            id = dto.id,
            taskType = taskType,
            createdDate = dto.createdDate,
            status = statusesMapper.getStatus(dto),
            ref = dto.ref,
            title = dto.subject,
            isBlocked = dto.isBlocked,
            tags = tagsMapper.toTags(dto.tags),
            isClosed = dto.isClosed,
            colors = dto.color?.let {
                persistentListOf(it)
            } ?: dto.epics.orEmpty().map {
                it.color
            }.toPersistentList(),
            assignee = dto.assignedToExtraInfo?.let { assigned ->
                userMapper.toUser(assigned)
            },
            project = projectMapper.toProjectExtraInfo(dto.projectDTOExtraInfo)
        )
    }
}
