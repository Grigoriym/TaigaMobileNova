package com.grappim.taigamobile.testing.dao

import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

class FakeProjectDao: ProjectDao {
    override suspend fun insert(project: ProjectEntity): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getProjectById(id: Long): ProjectEntity {
        TODO("Not yet implemented")
    }

    override fun getProjectByIdFlow(id: Long): Flow<ProjectEntity?> {
        TODO("Not yet implemented")
    }
}