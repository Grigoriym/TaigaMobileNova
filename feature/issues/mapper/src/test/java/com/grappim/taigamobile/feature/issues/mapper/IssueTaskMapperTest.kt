package com.grappim.taigamobile.feature.issues.mapper

import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.testing.getFiltersData
import com.grappim.taigamobile.testing.getPriority
import com.grappim.taigamobile.testing.getProject
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getSeverity
import com.grappim.taigamobile.testing.getStatus
import com.grappim.taigamobile.testing.getTag
import com.grappim.taigamobile.testing.getType
import com.grappim.taigamobile.testing.getUser
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class IssueTaskMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val userMapper: UserMapper = mockk()
    private val statusesMapper: StatusesMapper = mockk()
    private val projectMapper: ProjectMapper = mockk()
    private val tagsMapper: TagsMapper = mockk()
    private val dueDateStatusMapper: DueDateStatusMapper = mockk()

    private val serverStorage: ServerStorage = mockk()

    private lateinit var sut: IssueMapper

    @Before
    fun setup() {
        coEvery { serverStorage.server } returns "https://taiga.example.com"

        sut = IssueMapper(
            ioDispatcher = testDispatcher,
            userMapper = userMapper,
            statusesMapper = statusesMapper,
            projectMapper = projectMapper,
            tagsMapper = tagsMapper,
            dueDateStatusMapper = dueDateStatusMapper,
            serverStorage = serverStorage
        )
    }

    @Test
    fun `toDomain without filters should map basic fields correctly`() = runTest {
        val response = getWorkItemResponseDTO()
        val user = getUser()

        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(response.id, result.id)
        assertEquals(response.version, result.version)
        assertEquals(response.createdDate, result.createdDateTime)
        assertEquals(response.dueDate, result.dueDate)
        assertEquals(response.owner, result.creatorId)
        assertEquals(response.subject, result.title)
        assertEquals(response.description, result.description)
        assertEquals(response.ref, result.ref)
        assertEquals(response.isClosed, result.isClosed)
        assertEquals(response.milestone, result.milestone)
        assertEquals(DueDateStatus.DueSoon, result.dueDateStatus)
        assertEquals(user, result.assignee)
    }

    @Test
    fun `toDomain should map basic fields correctly`() = runTest {
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData()
        val user = getUser()

        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { statusesMapper.getStatus(response) } returns getStatus()
        coEvery { statusesMapper.getType(filtersData, response) } returns getType()
        coEvery { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        coEvery { statusesMapper.getPriority(filtersData, response) } returns getPriority()

        val result = sut.toDomain(response, filtersData)

        assertEquals(response.id, result.id)
        assertEquals(response.version, result.version)
        assertEquals(response.createdDate, result.createdDateTime)
        assertEquals(response.dueDate, result.dueDate)
        assertEquals(response.owner, result.creatorId)
        assertEquals(response.subject, result.title)
        assertEquals(response.description, result.description)
        assertEquals(response.ref, result.ref)
        assertEquals(response.isClosed, result.isClosed)
        assertEquals(response.milestone, result.milestone)
        assertEquals(DueDateStatus.DueSoon, result.dueDateStatus)
        assertEquals(user, result.assignee)
    }

    @Test
    fun `toDomain should handle null owner with error`() = runTest {
        val response = getWorkItemResponseDTO().copy(owner = null)
        val filtersData = getFiltersData()

        try {
            sut.toDomain(response, filtersData)
            assert(false) { "Expected error for null owner" }
        } catch (e: IllegalStateException) {
            assertEquals("Owner field is null", e.message)
        }
    }

    @Test
    fun `toDomain should map due date status correctly`() = runTest {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = DueDateStatusDTO.DueSoon)
        val filtersData = getFiltersData()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()
        coEvery { statusesMapper.getType(filtersData, response) } returns getType()
        coEvery { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        coEvery { statusesMapper.getPriority(filtersData, response) } returns getPriority()

        val result = sut.toDomain(response, filtersData)

        assertNotNull(result.dueDateStatus)
    }

    @Test
    fun `toDomain should handle blocked note correctly`() = runTest {
        val user = getUser()
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )
        val filtersData = getFiltersData()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()
        coEvery { statusesMapper.getType(filtersData, response) } returns getType()
        coEvery { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        coEvery { statusesMapper.getPriority(filtersData, response) } returns getPriority()

        val result = sut.toDomain(response, filtersData)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() = runTest {
        val user = getUser()
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()
        coEvery { statusesMapper.getType(filtersData, response) } returns getType()
        coEvery { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        coEvery { statusesMapper.getPriority(filtersData, response) } returns getPriority()

        val result = sut.toDomain(response, filtersData)

        val expectedUrl =
            "https://taiga.example.com/project/${response.projectDTOExtraInfo.slug}/issue/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly from nested list`() = runTest {
        val user = getUser()
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData()

        val firstTag = getTag()
        val secondTag = getTag()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(
            firstTag,
            secondTag
        )
        coEvery { statusesMapper.getStatus(response) } returns getStatus()
        coEvery { statusesMapper.getType(filtersData, response) } returns getType()
        coEvery { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        coEvery { statusesMapper.getPriority(filtersData, response) } returns getPriority()

        val result = sut.toDomain(response, filtersData)

        assertEquals(2, result.tags.size)
    }
}
