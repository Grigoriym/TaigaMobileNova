package com.grappim.taigamobile.core.storage.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.db.entities.WORK_ITEM_TABLE
import com.grappim.taigamobile.core.storage.db.entities.WorkItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WorkItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WorkItemEntity)

    @Query("SELECT * FROM $WORK_ITEM_TABLE WHERE projectId = :projectId ORDER BY createdDate DESC")
    fun getByProjectId(projectId: Long): Flow<List<WorkItemEntity>>

    @Query(
        "SELECT * FROM $WORK_ITEM_TABLE WHERE projectId = :projectId AND taskType = :taskType ORDER BY createdDate DESC"
    )
    fun getByProjectIdAndType(projectId: Long, taskType: CommonTaskType): Flow<List<WorkItemEntity>>

    @Query(
        "SELECT * FROM $WORK_ITEM_TABLE WHERE projectId = :projectId AND sprintId = :sprintId ORDER BY createdDate DESC"
    )
    fun getByProjectIdAndSprint(projectId: Long, sprintId: Long): Flow<List<WorkItemEntity>>

    @Query(
        "SELECT * FROM $WORK_ITEM_TABLE WHERE projectId = :projectId AND sprintId IS NULL AND taskType = :taskType ORDER BY createdDate DESC"
    )
    fun getBacklogItems(projectId: Long, taskType: CommonTaskType): Flow<List<WorkItemEntity>>

    @Query("SELECT * FROM $WORK_ITEM_TABLE WHERE assigneeId = :userId ORDER BY createdDate DESC")
    fun getAssignedToUser(userId: Long): Flow<List<WorkItemEntity>>

    @Query("SELECT * FROM $WORK_ITEM_TABLE WHERE id = :id AND taskType = :taskType")
    suspend fun getById(id: Long, taskType: CommonTaskType): WorkItemEntity?

    @Query("DELETE FROM $WORK_ITEM_TABLE WHERE projectId = :projectId")
    suspend fun deleteByProjectId(projectId: Long)

    @Query("DELETE FROM $WORK_ITEM_TABLE WHERE projectId = :projectId AND taskType = :taskType")
    suspend fun deleteByProjectIdAndType(projectId: Long, taskType: CommonTaskType)

    @Query("DELETE FROM $WORK_ITEM_TABLE WHERE cachedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query(
        "SELECT * FROM $WORK_ITEM_TABLE WHERE projectId = :projectId AND taskType = :taskType ORDER BY createdDate DESC"
    )
    fun pagingSource(projectId: Long, taskType: CommonTaskType): PagingSource<Int, WorkItemEntity>
}
