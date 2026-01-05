package com.grappim.taigamobile.core.storage.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Query("SELECT * FROM project_table WHERE id =:id")
    suspend fun getProjectById(id: Long): ProjectEntity

    @Query("SELECT * FROM project_table WHERE id =:id")
    fun getProjectByIdFlow(id: Long): Flow<ProjectEntity?>
}
