package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.feature.projects.domain.ProjectValueItem
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.feature.projects.domain.ProjectValuesRepository
import com.grappim.taigamobile.feature.projects.dto.ProjectValueItemDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectValueRequestDTO
import com.grappim.taigamobile.testing.api.FakeProjectValuesApi
import com.grappim.taigamobile.testing.models.getProjectValueItemDTO
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.assertFailsWithTestException
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The interesting logic here is that `createProjectValue` and `updateProjectValue` null out every
 * field the [ProjectValueType] does not declare. The three types used below are chosen so that
 * every one of those five flags is exercised both ways:
 *
 * | type            | hasColor | hasClosed | hasArchived | hasValue | hasDaysToDue |
 * |-----------------|----------|-----------|-------------|----------|--------------|
 * | `US_STATUSES`   | true     | true      | true        | false    | false        |
 * | `POINTS`        | false    | false     | false       | true     | false        |
 * | `US_DUE_DATES`  | true     | false     | false       | false    | true         |
 */
internal class ProjectValuesRepositoryImplTest {
    private val projectValuesApi = FakeProjectValuesApi()
    private val taigaSessionStorage = FakeTaigaSessionStorage()

    private val sut: ProjectValuesRepository = ProjectValuesRepositoryImpl(
        projectValuesApi = projectValuesApi,
        taigaSessionStorage = taigaSessionStorage
    )

    private val color = "#FF0000"
    private val value = 8.0
    private val daysToDue = 14

    @Test
    fun `on getProjectValues returns mapped items for the current project`() = runTest {
        val projectId = getRandomLong()
        val dtos = listOf(getProjectValueItemDTO(), getProjectValueItemDTO())
        taigaSessionStorage.currentProjectId = projectId
        projectValuesApi.getProjectValuesResult = dtos

        val actual = sut.getProjectValues(ProjectValueType.US_STATUSES)

        assertContentEquals(dtos.map { it.expectedDomain() }, actual)
        assertEquals(
            Pair(ProjectValueType.US_STATUSES.endpoint, projectId),
            projectValuesApi.getProjectValuesCalls.single()
        )
    }

    @Test
    fun `on getProjectValues returns empty list when api returns nothing`() = runTest {
        projectValuesApi.getProjectValuesResult = emptyList()

        assertTrue(sut.getProjectValues(ProjectValueType.POINTS).isEmpty())
    }

    @Test
    fun `on createProjectValue for a type with color closed and archived sends them`() = runTest {
        val projectId = getRandomLong()
        val name = getRandomString()
        val dto = getProjectValueItemDTO()
        taigaSessionStorage.currentProjectId = projectId
        projectValuesApi.createProjectValueResult = dto

        val actual = sut.createProjectValue(
            type = ProjectValueType.US_STATUSES,
            name = name,
            color = color,
            isClosed = true,
            isArchived = true,
            value = value,
            daysToDue = daysToDue
        )

        assertEquals(dto.expectedDomain(), actual)
        val call = projectValuesApi.createProjectValueCalls.single()
        assertEquals(ProjectValueType.US_STATUSES.endpoint, call.first)
        assertEquals(
            ProjectValueRequestDTO(
                project = projectId,
                name = name,
                color = color,
                isClosed = true,
                isArchived = true,
                value = null,
                daysToDue = null
            ),
            call.second
        )
    }

    @Test
    fun `on createProjectValue for a type with value only sends value`() = runTest {
        val projectId = getRandomLong()
        val name = getRandomString()
        taigaSessionStorage.currentProjectId = projectId
        projectValuesApi.createProjectValueResult = getProjectValueItemDTO()

        sut.createProjectValue(
            type = ProjectValueType.POINTS,
            name = name,
            color = color,
            isClosed = true,
            isArchived = true,
            value = value,
            daysToDue = daysToDue
        )

        assertEquals(
            ProjectValueRequestDTO(
                project = projectId,
                name = name,
                color = null,
                isClosed = null,
                isArchived = null,
                value = value,
                daysToDue = null
            ),
            projectValuesApi.createProjectValueCalls.single().second
        )
    }

    @Test
    fun `on createProjectValue for a due-date type sends color and daysToDue`() = runTest {
        val projectId = getRandomLong()
        val name = getRandomString()
        taigaSessionStorage.currentProjectId = projectId
        projectValuesApi.createProjectValueResult = getProjectValueItemDTO()

        sut.createProjectValue(
            type = ProjectValueType.US_DUE_DATES,
            name = name,
            color = color,
            isClosed = true,
            isArchived = true,
            value = value,
            daysToDue = daysToDue
        )

        assertEquals(
            ProjectValueRequestDTO(
                project = projectId,
                name = name,
                color = color,
                isClosed = null,
                isArchived = null,
                value = null,
                daysToDue = daysToDue
            ),
            projectValuesApi.createProjectValueCalls.single().second
        )
    }

    @Test
    fun `on updateProjectValue for a type with color closed and archived sends them without project`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val dto = getProjectValueItemDTO()
        projectValuesApi.updateProjectValueResult = dto

        val actual = sut.updateProjectValue(
            type = ProjectValueType.US_STATUSES,
            id = id,
            name = name,
            color = color,
            isClosed = true,
            isArchived = true,
            value = value,
            daysToDue = daysToDue
        )

        assertEquals(dto.expectedDomain(), actual)
        val call = projectValuesApi.updateProjectValueCalls.single()
        assertEquals(ProjectValueType.US_STATUSES.endpoint, call.first)
        assertEquals(id, call.second)
        assertEquals(
            ProjectValueRequestDTO(
                project = null,
                name = name,
                color = color,
                isClosed = true,
                isArchived = true,
                value = null,
                daysToDue = null
            ),
            call.third
        )
    }

    @Test
    fun `on updateProjectValue for a type with value only sends value`() = runTest {
        val name = getRandomString()
        projectValuesApi.updateProjectValueResult = getProjectValueItemDTO()

        sut.updateProjectValue(
            type = ProjectValueType.POINTS,
            id = getRandomLong(),
            name = name,
            color = color,
            isClosed = true,
            isArchived = true,
            value = value,
            daysToDue = daysToDue
        )

        assertEquals(
            ProjectValueRequestDTO(
                project = null,
                name = name,
                color = null,
                isClosed = null,
                isArchived = null,
                value = value,
                daysToDue = null
            ),
            projectValuesApi.updateProjectValueCalls.single().third
        )
    }

    @Test
    fun `on updateProjectValue for a due-date type sends color and daysToDue`() = runTest {
        val name = getRandomString()
        projectValuesApi.updateProjectValueResult = getProjectValueItemDTO()

        sut.updateProjectValue(
            type = ProjectValueType.US_DUE_DATES,
            id = getRandomLong(),
            name = name,
            color = color,
            isClosed = true,
            isArchived = true,
            value = value,
            daysToDue = daysToDue
        )

        assertEquals(
            ProjectValueRequestDTO(
                project = null,
                name = name,
                color = color,
                isClosed = null,
                isArchived = null,
                value = null,
                daysToDue = daysToDue
            ),
            projectValuesApi.updateProjectValueCalls.single().third
        )
    }

    @Test
    fun `on deleteProjectValue calls api with moveTo`() = runTest {
        val id = getRandomLong()
        val moveTo = getRandomLong()

        sut.deleteProjectValue(ProjectValueType.US_STATUSES, id, moveTo)

        assertEquals(
            Triple(ProjectValueType.US_STATUSES.endpoint, id, moveTo),
            projectValuesApi.deleteProjectValueCalls.single()
        )
    }

    @Test
    fun `on deleteProjectValue calls api without moveTo`() = runTest {
        val id = getRandomLong()

        sut.deleteProjectValue(ProjectValueType.US_DUE_DATES, id, null)

        assertEquals(
            Triple(ProjectValueType.US_DUE_DATES.endpoint, id, null),
            projectValuesApi.deleteProjectValueCalls.single()
        )
    }

    // Failure paths — one per public method. See CLAUDE.md, Testing.

    @Test
    fun `on getProjectValues error propagates`() = runTest {
        projectValuesApi.errorToThrow = testException

        assertFailsWithTestException { sut.getProjectValues(ProjectValueType.US_STATUSES) }
    }

    @Test
    fun `on createProjectValue error propagates`() = runTest {
        projectValuesApi.errorToThrow = testException

        assertFailsWithTestException {
            sut.createProjectValue(
                type = ProjectValueType.US_STATUSES,
                name = getRandomString(),
                color = color,
                isClosed = false,
                isArchived = false,
                value = null,
                daysToDue = null
            )
        }
    }

    @Test
    fun `on updateProjectValue error propagates`() = runTest {
        projectValuesApi.errorToThrow = testException

        assertFailsWithTestException {
            sut.updateProjectValue(
                type = ProjectValueType.US_STATUSES,
                id = getRandomLong(),
                name = getRandomString(),
                color = color,
                isClosed = false,
                isArchived = false,
                value = null,
                daysToDue = null
            )
        }
    }

    @Test
    fun `on deleteProjectValue error propagates`() = runTest {
        projectValuesApi.errorToThrow = testException

        assertFailsWithTestException {
            sut.deleteProjectValue(ProjectValueType.US_STATUSES, getRandomLong(), null)
        }
    }

    private fun ProjectValueItemDTO.expectedDomain() = ProjectValueItem(
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
