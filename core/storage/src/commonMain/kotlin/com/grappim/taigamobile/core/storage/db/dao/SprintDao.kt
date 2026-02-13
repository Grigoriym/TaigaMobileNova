package com.grappim.taigamobile.core.storage.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grappim.taigamobile.core.storage.db.entities.SPRINT_TABLE
import com.grappim.taigamobile.core.storage.db.entities.SprintEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SprintDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sprints: List<SprintEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sprint: SprintEntity)

    @Query("SELECT * FROM $SPRINT_TABLE WHERE projectId = :projectId ORDER BY `order` ASC")
    fun getByProjectId(projectId: Long): Flow<List<SprintEntity>>

    @Query("SELECT * FROM $SPRINT_TABLE WHERE id = :id")
    suspend fun getById(id: Long): SprintEntity?

    @Query("DELETE FROM $SPRINT_TABLE WHERE projectId = :projectId")
    suspend fun deleteByProjectId(projectId: Long)

    @Query("DELETE FROM $SPRINT_TABLE WHERE cachedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("SELECT * FROM $SPRINT_TABLE WHERE projectId = :projectId AND isClosed = :isClosed ORDER BY `order` ASC")
    fun pagingSource(projectId: Long, isClosed: Boolean): PagingSource<Int, SprintEntity>

    @Query("DELETE FROM $SPRINT_TABLE WHERE projectId = :projectId AND isClosed = :isClosed")
    suspend fun deleteByProjectIdAndClosed(projectId: Long, isClosed: Boolean)
}
