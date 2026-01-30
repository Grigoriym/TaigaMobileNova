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
import com.grappim.taigamobile.testing.getProjectExtraInfo
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getSeverity
import com.grappim.taigamobile.testing.getStatus
import com.grappim.taigamobile.testing.getTag
import com.grappim.taigamobile.testing.getType
import com.grappim.taigamobile.testing.getUser
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IssueTaskMapperTest {

    private val userMapper: UserMapper = mockk()
    private val statusesMapper: StatusesMapper = mockk()
    private val projectMapper: ProjectMapper = mockk()
    private val tagsMapper: TagsMapper = mockk()
    private val dueDateStatusMapper: DueDateStatusMapper = mockk()

    private val serverStorage: ServerStorage = mockk()

    private lateinit var sut: IssueMapper

    @Before
    fun setup() {
        every { serverStorage.server } returns "https://taiga.example.com"

        sut = IssueMapper(
            userMapper = userMapper,
            statusesMapper = statusesMapper,
            projectMapper = projectMapper,
            tagsMapper = tagsMapper,
            dueDateStatusMapper = dueDateStatusMapper,
            serverStorage = serverStorage
        )
    }

    @Test
    fun `toDomain without filters should map basic fields correctly`() {
        val response = getWorkItemResponseDTO()
        val user = getUser()

        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { statusesMapper.getStatus(response) } returns getStatus()

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
    fun `toDomain should map basic fields correctly`() {
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData()
        val user = getUser()

        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { statusesMapper.getStatus(response) } returns getStatus()
        every { statusesMapper.getType(filtersData, response) } returns getType()
        every { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        every { statusesMapper.getPriority(filtersData, response) } returns getPriority()

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
    fun `toDomain should handle null owner with error`() {
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
    fun `toDomain should map due date status correctly`() {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = DueDateStatusDTO.DueSoon)
        val filtersData = getFiltersData()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()
        every { statusesMapper.getType(filtersData, response) } returns getType()
        every { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        every { statusesMapper.getPriority(filtersData, response) } returns getPriority()

        val result = sut.toDomain(response, filtersData)

        assertNotNull(result.dueDateStatus)
    }

    @Test
    fun `toDomain should handle blocked note correctly`() {
        val user = getUser()
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )
        val filtersData = getFiltersData()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()
        every { statusesMapper.getType(filtersData, response) } returns getType()
        every { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        every { statusesMapper.getPriority(filtersData, response) } returns getPriority()

        val result = sut.toDomain(response, filtersData)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() {
        val user = getUser()
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()
        every { statusesMapper.getType(filtersData, response) } returns getType()
        every { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        every { statusesMapper.getPriority(filtersData, response) } returns getPriority()

        val result = sut.toDomain(response, filtersData)

        val expectedUrl =
            "https://taiga.example.com/project/${response.projectDTOExtraInfo.slug}/issue/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly from nested list`() {
        val user = getUser()
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData()

        val firstTag = getTag()
        val secondTag = getTag()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(
            firstTag,
            secondTag
        )
        every { statusesMapper.getStatus(response) } returns getStatus()
        every { statusesMapper.getType(filtersData, response) } returns getType()
        every { statusesMapper.getSeverity(filtersData, response) } returns getSeverity()
        every { statusesMapper.getPriority(filtersData, response) } returns getPriority()

        val result = sut.toDomain(response, filtersData)

        assertEquals(2, result.tags.size)
    }
}
