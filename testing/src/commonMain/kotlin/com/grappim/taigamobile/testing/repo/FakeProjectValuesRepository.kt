package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.feature.projects.domain.ProjectValueItem
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.feature.projects.domain.ProjectValuesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class FakeProjectValuesRepository : ProjectValuesRepository {

    data class SaveCall(
        val type: ProjectValueType,
        val id: Long?,
        val name: String,
        val color: String?,
        val isClosed: Boolean,
        val isArchived: Boolean,
        val value: Double?,
        val daysToDue: Int?
    )

    data class DeleteCall(val type: ProjectValueType, val id: Long, val moveTo: Long?)

    var getProjectValuesResult: ImmutableList<ProjectValueItem> = persistentListOf()
    var getProjectValuesThrows: Throwable? = null
    val getProjectValuesCalls: MutableList<ProjectValueType> = mutableListOf()

    var createProjectValueResult: ProjectValueItem? = null
    var createProjectValueThrows: Throwable? = null
    val createProjectValueCalls: MutableList<SaveCall> = mutableListOf()

    var updateProjectValueResult: ProjectValueItem? = null
    var updateProjectValueThrows: Throwable? = null
    val updateProjectValueCalls: MutableList<SaveCall> = mutableListOf()

    var deleteProjectValueThrows: Throwable? = null
    val deleteProjectValueCalls: MutableList<DeleteCall> = mutableListOf()

    override suspend fun getProjectValues(type: ProjectValueType): ImmutableList<ProjectValueItem> {
        getProjectValuesCalls.add(type)
        getProjectValuesThrows?.let { throw it }
        return getProjectValuesResult
    }

    override suspend fun createProjectValue(
        type: ProjectValueType,
        name: String,
        color: String?,
        isClosed: Boolean,
        isArchived: Boolean,
        value: Double?,
        daysToDue: Int?
    ): ProjectValueItem {
        createProjectValueCalls.add(
            SaveCall(
                type = type,
                id = null,
                name = name,
                color = color,
                isClosed = isClosed,
                isArchived = isArchived,
                value = value,
                daysToDue = daysToDue
            )
        )
        createProjectValueThrows?.let { throw it }
        return createProjectValueResult ?: error("createProjectValueResult not set")
    }

    override suspend fun updateProjectValue(
        type: ProjectValueType,
        id: Long,
        name: String,
        color: String?,
        isClosed: Boolean,
        isArchived: Boolean,
        value: Double?,
        daysToDue: Int?
    ): ProjectValueItem {
        updateProjectValueCalls.add(
            SaveCall(
                type = type,
                id = id,
                name = name,
                color = color,
                isClosed = isClosed,
                isArchived = isArchived,
                value = value,
                daysToDue = daysToDue
            )
        )
        updateProjectValueThrows?.let { throw it }
        return updateProjectValueResult ?: error("updateProjectValueResult not set")
    }

    override suspend fun deleteProjectValue(type: ProjectValueType, id: Long, moveTo: Long?) {
        deleteProjectValueCalls.add(DeleteCall(type = type, id = id, moveTo = moveTo))
        deleteProjectValueThrows?.let { throw it }
    }
}
