package com.grappim.taigamobile.testing.dao

import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

class FakeProjectDao : ProjectDao {
    val projectsById: MutableMap<Long, ProjectEntity> = mutableMapOf()
    val insertCalls: MutableList<ProjectEntity> = mutableListOf()

    override suspend fun insert(project: ProjectEntity): Long {
        insertCalls += project
        return 0L
    }

    override suspend fun getProjectById(id: Long): ProjectEntity =
        projectsById[id] ?: error("No project found for id=$id")

    override fun getProjectByIdFlow(id: Long): Flow<ProjectEntity?> {
        TODO("Not yet implemented")
    }
}
