package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.projects.domain.ProjectValueItem
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.feature.projects.domain.ProjectValuesRepository
import com.grappim.taigamobile.feature.projects.dto.ProjectValueItemDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectValueRequestDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Single

@Single(binds = [ProjectValuesRepository::class])
class ProjectValuesRepositoryImpl(
    private val projectValuesApi: ProjectValuesApi,
    private val taigaSessionStorage: TaigaSessionStorage
) : ProjectValuesRepository {

    override suspend fun getProjectValues(type: ProjectValueType): ImmutableList<ProjectValueItem> =
        projectValuesApi
            .getProjectValues(type.endpoint, taigaSessionStorage.getCurrentProjectId())
            .map { it.toDomain() }
            .toImmutableList()

    override suspend fun createProjectValue(
        type: ProjectValueType,
        name: String,
        color: String?,
        isClosed: Boolean,
        isArchived: Boolean,
        value: Double?,
        daysToDue: Int?
    ): ProjectValueItem = projectValuesApi.createProjectValue(
        endpoint = type.endpoint,
        request = ProjectValueRequestDTO(
            project = taigaSessionStorage.getCurrentProjectId(),
            name = name,
            color = if (type.hasColor) color else null,
            isClosed = if (type.hasClosed) isClosed else null,
            isArchived = if (type.hasArchived) isArchived else null,
            value = if (type.hasValue) value else null,
            daysToDue = if (type.hasDaysToDue) daysToDue else null
        )
    ).toDomain()

    override suspend fun updateProjectValue(
        type: ProjectValueType,
        id: Long,
        name: String,
        color: String?,
        isClosed: Boolean,
        isArchived: Boolean,
        value: Double?,
        daysToDue: Int?
    ): ProjectValueItem = projectValuesApi.updateProjectValue(
        endpoint = type.endpoint,
        id = id,
        request = ProjectValueRequestDTO(
            name = name,
            color = if (type.hasColor) color else null,
            isClosed = if (type.hasClosed) isClosed else null,
            isArchived = if (type.hasArchived) isArchived else null,
            value = if (type.hasValue) value else null,
            daysToDue = if (type.hasDaysToDue) daysToDue else null
        )
    ).toDomain()

    override suspend fun deleteProjectValue(type: ProjectValueType, id: Long, moveTo: Long?) {
        projectValuesApi.deleteProjectValue(type.endpoint, id, moveTo)
    }

    private fun ProjectValueItemDTO.toDomain() = ProjectValueItem(
        id = id,
        name = name,
        color = color,
        order = order,
        project = project,
        isClosed = isClosed,
        isArchived = isArchived,
        value = value,
        daysToDue = daysToDue,
        byDefault = byDefault
    )
}
