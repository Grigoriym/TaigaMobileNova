package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.epics.dto.EpicShortInfoDTO
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.testing.models.getProjectExtraInfo
import com.grappim.taigamobile.testing.models.getStatus
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkItemMapperTest {

    private val statusesMapper: StatusesMapper = StatusesMapper()
    private val userMapper: UserMapper = UserMapper()
    private val tagsMapper: TagsMapper = TagsMapper()
    private val projectMapper: ProjectMapper = ProjectMapper()

    private lateinit var sut: WorkItemMapper

    @BeforeTest
    fun setup() {
        sut = WorkItemMapper(
            statusesMapper = statusesMapper,
            userMapper = userMapper,
            tagsMapper = tagsMapper,
            projectMapper = projectMapper
        )
    }

    @Test
    fun `toUpdateDomain should map watchers correctly`() = runTest {
        val watcherIds = listOf(1L, 2L, 3L)
        val dto = getWorkItemResponseDTO().copy(watchers = watcherIds)

        val result = sut.toUpdateDomain(dto)

        assertEquals(watcherIds, result.watcherUserIds)
    }

    @Test
    fun `toUpdateDomain should handle null watchers`() = runTest {
        val dto = getWorkItemResponseDTO().copy(watchers = null)

        val result = sut.toUpdateDomain(dto)

        assertTrue(result.watcherUserIds.isEmpty())
    }

    @Test
    fun `toUpdateDomain should handle empty watchers`() = runTest {
        val dto = getWorkItemResponseDTO().copy(watchers = emptyList())

        val result = sut.toUpdateDomain(dto)

        assertTrue(result.watcherUserIds.isEmpty())
    }

    @Test
    fun `toDomain should map basic fields correctly`() = runTest {
        val dto = getWorkItemResponseDTO()
        val taskType = CommonTaskType.UserStory
        val status = getStatus()
        val user = getUser()
        val projectExtraInfo = getProjectExtraInfo()

        val result = sut.toDomain(dto, taskType)

        assertEquals(dto.id, result.id)
        assertEquals(taskType, result.taskType)
        assertEquals(dto.createdDate, result.createdDate)
        assertEquals(status, result.status)
        assertEquals(dto.ref, result.ref)
        assertEquals(dto.subject, result.title)
        assertEquals(dto.isBlocked, result.isBlocked)
        assertEquals(dto.isClosed, result.isClosed)
        assertEquals(user, result.assignee)
        assertEquals(projectExtraInfo, result.project)
    }

    @Test
    fun `toDomain should handle null assignedToExtraInfo`() = runTest {
        val dto = getWorkItemResponseDTO().copy(assignedToExtraInfo = null)
        val taskType = CommonTaskType.Task
        val result = sut.toDomain(dto, taskType)

        assertNull(result.assignee)
    }

    @Test
    fun `toDomain should use color when present`() = runTest {
        val color = "#FF5733"
        val dto = getWorkItemResponseDTO().copy(color = color, epics = null)
        val taskType = CommonTaskType.Issue

        val result = sut.toDomain(dto, taskType)

        assertEquals(1, result.colors.size)
        assertEquals(color, result.colors[0])
    }

    @Test
    fun `toDomain should use epic colors when color is null`() = runTest {
        val epic1 = EpicShortInfoDTO(
            id = getRandomLong(),
            title = getRandomString(),
            ref = getRandomLong(),
            color = "#FF0000"
        )
        val epic2 = EpicShortInfoDTO(
            id = getRandomLong(),
            title = getRandomString(),
            ref = getRandomLong(),
            color = "#00FF00"
        )
        val dto = getWorkItemResponseDTO().copy(color = null, epics = listOf(epic1, epic2))
        val taskType = CommonTaskType.UserStory

        val result = sut.toDomain(dto, taskType)

        assertEquals(2, result.colors.size)
        assertEquals("#FF0000", result.colors[0])
        assertEquals("#00FF00", result.colors[1])
    }

    @Test
    fun `toDomain should return empty colors when both color and epics are null`() = runTest {
        val dto = getWorkItemResponseDTO().copy(color = null, epics = null)
        val taskType = CommonTaskType.Task

        val result = sut.toDomain(dto, taskType)

        assertTrue(result.colors.isEmpty())
    }

    @Test
    fun `toDomainList should map list of DTOs correctly`() = runTest {
        val dto1 = getWorkItemResponseDTO()
        val dto2 = getWorkItemResponseDTO()
        val dtos = listOf(dto1, dto2)
        val taskType = CommonTaskType.Issue

        val result = sut.toDomainList(dtos, taskType)

        assertEquals(2, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto2.id, result[1].id)
        assertEquals(taskType, result[0].taskType)
        assertEquals(taskType, result[1].taskType)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() = runTest {
        val result = sut.toDomainList(emptyList(), CommonTaskType.UserStory)

        assertTrue(result.isEmpty())
    }
}
