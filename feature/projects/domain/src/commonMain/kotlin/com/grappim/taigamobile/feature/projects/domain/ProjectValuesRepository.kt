package com.grappim.taigamobile.feature.projects.domain

import kotlinx.collections.immutable.ImmutableList

interface ProjectValuesRepository {
    suspend fun getProjectValues(type: ProjectValueType): ImmutableList<ProjectValueItem>

    suspend fun createProjectValue(
        type: ProjectValueType,
        name: String,
        color: String?,
        isClosed: Boolean,
        isArchived: Boolean,
        value: Double?,
        daysToDue: Int?
    ): ProjectValueItem

    suspend fun updateProjectValue(
        type: ProjectValueType,
        id: Long,
        name: String,
        color: String?,
        isClosed: Boolean,
        isArchived: Boolean,
        value: Double?,
        daysToDue: Int?
    ): ProjectValueItem

    suspend fun deleteProjectValue(type: ProjectValueType, id: Long, moveTo: Long?)
}
