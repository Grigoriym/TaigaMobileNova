package com.grappim.taigamobile.testing.dao

import androidx.paging.PagingSource
import com.grappim.taigamobile.core.storage.db.dao.SprintDao
import com.grappim.taigamobile.core.storage.db.entities.SprintEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSprintDao : SprintDao {

    var insertedAll: List<SprintEntity>? = null
    var insertAllThrows: Throwable? = null

    override suspend fun insertAll(sprints: List<SprintEntity>) {
        insertAllThrows?.let { throw it }
        insertedAll = sprints
    }

    override suspend fun insert(sprint: SprintEntity) = error("not used in this test")

    var sprintsByProjectId: List<SprintEntity> = emptyList()

    override fun getByProjectId(projectId: Long): Flow<List<SprintEntity>> = flowOf(sprintsByProjectId)

    override suspend fun getById(id: Long): SprintEntity? = error("not used in this test")

    override suspend fun deleteByProjectId(projectId: Long) = Unit

    override suspend fun deleteOlderThan(timestamp: Long) = error("not used in this test")

    override fun pagingSource(projectId: Long, isClosed: Boolean): PagingSource<Int, SprintEntity> =
        error("not used in this test")

    var deletedByProjectIdAndClosed: Pair<Long, Boolean>? = null
    var deleteByProjectIdAndClosedThrows: Throwable? = null

    override suspend fun deleteByProjectIdAndClosed(projectId: Long, isClosed: Boolean) {
        deleteByProjectIdAndClosedThrows?.let { throw it }
        deletedByProjectIdAndClosed = projectId to isClosed
    }
}
