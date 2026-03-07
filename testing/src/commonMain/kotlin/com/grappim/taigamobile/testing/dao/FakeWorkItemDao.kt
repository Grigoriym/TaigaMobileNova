package com.grappim.taigamobile.testing.dao

import androidx.paging.PagingSource
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.core.storage.db.entities.WorkItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeWorkItemDao : WorkItemDao {

    override suspend fun insertAll(items: List<WorkItemEntity>) = Unit

    override suspend fun insert(item: WorkItemEntity) = error("not used in this test")

    override fun getByProjectId(projectId: Long): Flow<List<WorkItemEntity>> =
        error("not used in this test")

    override fun getByProjectIdAndType(
        projectId: Long,
        taskType: CommonTaskType
    ): Flow<List<WorkItemEntity>> = error("not used in this test")

    var workItemsByProjectIdAndSprint: List<WorkItemEntity> = emptyList()

    override fun getByProjectIdAndSprint(
        projectId: Long,
        sprintId: Long
    ): Flow<List<WorkItemEntity>> = flowOf(workItemsByProjectIdAndSprint)

    override fun getBacklogItems(
        projectId: Long,
        taskType: CommonTaskType
    ): Flow<List<WorkItemEntity>> = error("not used in this test")

    override fun getAssignedToUser(userId: Long): Flow<List<WorkItemEntity>> =
        error("not used in this test")

    override suspend fun getById(id: Long, taskType: CommonTaskType): WorkItemEntity? =
        error("not used in this test")

    override suspend fun deleteByProjectId(projectId: Long) = error("not used in this test")

    override suspend fun deleteByProjectIdAndType(projectId: Long, taskType: CommonTaskType) = Unit

    override suspend fun deleteOlderThan(timestamp: Long) = error("not used in this test")

    override fun pagingSource(
        projectId: Long,
        taskType: CommonTaskType
    ): PagingSource<Int, WorkItemEntity> = error("not used in this test")
}
