package com.grappim.taigamobile.testing.dao

import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeProjectDao : ProjectDao {
    val projectsById: MutableMap<Long, ProjectEntity> = mutableMapOf()
    val insertCalls: MutableList<ProjectEntity> = mutableListOf()

    var errorToThrow: Throwable? = null

    /**
     * Emissions of [getProjectByIdFlow], keyed by the requested id. An id with no entry emits
     * nothing, which is how a test exercises the `filterNotNull` in a consumer.
     */
    val projectFlowsById: MutableMap<Long, List<ProjectEntity?>> = mutableMapOf()
    val getProjectByIdFlowCalls: MutableList<Long> = mutableListOf()

    override suspend fun insert(project: ProjectEntity): Long {
        errorToThrow?.let { throw it }
        insertCalls += project
        return 0L
    }

    override suspend fun getProjectById(id: Long): ProjectEntity {
        errorToThrow?.let { throw it }
        return projectsById[id] ?: error("No project found for id=$id")
    }

    override fun getProjectByIdFlow(id: Long): Flow<ProjectEntity?> {
        errorToThrow?.let { throw it }
        getProjectByIdFlowCalls += id
        return flowOf(*(projectFlowsById[id] ?: emptyList()).toTypedArray())
    }

    override suspend fun deleteAll() = error("not used in this test")
}
