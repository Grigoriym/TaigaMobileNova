package com.grappim.taigamobile.testing.dao

import androidx.paging.PagingSource
import com.grappim.taigamobile.core.storage.db.dao.SprintDao
import com.grappim.taigamobile.core.storage.db.entities.SprintEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSprintDao : SprintDao {

    override suspend fun insertAll(sprints: List<SprintEntity>) = Unit

    override suspend fun insert(sprint: SprintEntity) = error("not used in this test")

    var sprintsByProjectId: List<SprintEntity> = emptyList()

    override fun getByProjectId(projectId: Long): Flow<List<SprintEntity>> = flowOf(sprintsByProjectId)

    override suspend fun getById(id: Long): SprintEntity? = error("not used in this test")

    override suspend fun deleteByProjectId(projectId: Long) = Unit

    override suspend fun deleteOlderThan(timestamp: Long) = error("not used in this test")

    override fun pagingSource(projectId: Long, isClosed: Boolean): PagingSource<Int, SprintEntity> =
        error("not used in this test")

    override suspend fun deleteByProjectIdAndClosed(projectId: Long, isClosed: Boolean) =
        error("not used in this test")
}
