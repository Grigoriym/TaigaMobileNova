package com.grappim.taigamobile.feature.tasks.mapper

import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.domain.UserStoryShortInfo
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.feature.workitem.mapper.UserStoryShortInfoMapper
import com.grappim.taigamobile.testing.getProjectExtraInfo
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getStatus
import com.grappim.taigamobile.testing.getTag
import com.grappim.taigamobile.testing.getUser
import com.grappim.taigamobile.testing.getUserStoryShortInfoDTO
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TaskMapperTest {

    private val userMapper: UserMapper = mockk()
    private val statusesMapper: StatusesMapper = mockk()
    private val projectMapper: ProjectMapper = mockk()
    private val tagsMapper: TagsMapper = mockk()
    private val dueDateStatusMapper: DueDateStatusMapper = mockk()
    private val userStoryShortInfoMapper: UserStoryShortInfoMapper = mockk()
    private val serverStorage: ServerStorage = mockk()

    private lateinit var sut: TaskMapper

    @Before
    fun setup() {
        coEvery { serverStorage.server } returns "https://taiga.example.com"

        sut = TaskMapper(
            serverStorage = serverStorage,
            userMapper = userMapper,
            statusesMapper = statusesMapper,
            projectMapper = projectMapper,
            tagsMapper = tagsMapper,
            dueDateStatusMapper = dueDateStatusMapper,
            userStoryShortInfoMapper = userStoryShortInfoMapper
        )
    }

    @Test
    fun `toDomain should map basic fields correctly`() {
        val response = getWorkItemResponseDTO()
        val user = getUser()

        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
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
    fun `toDomain should handle null owner with error`() {
        val response = getWorkItemResponseDTO().copy(owner = null)

        try {
            sut.toDomain(response)
            assert(false) { "Expected error for null owner" }
        } catch (e: IllegalStateException) {
            assertEquals("Owner field is null", e.message)
        }
    }

    @Test
    fun `toDomain should map due date status correctly`() {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = DueDateStatusDTO.DueSoon)

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertNotNull(result.dueDateStatus)
        assertEquals(DueDateStatus.DueSoon, result.dueDateStatus)
    }

    @Test
    fun `toDomain should handle blocked note correctly when blocked`() {
        val user = getUser()
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should not include blocked note when not blocked`() {
        val user = getUser()
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = false,
            blockedNote = blockedNote
        )

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertNull(result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() {
        val user = getUser()
        val response = getWorkItemResponseDTO()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        val expectedUrl =
            "https://taiga.example.com/project/${response.projectDTOExtraInfo.slug}/task/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly`() {
        val user = getUser()
        val response = getWorkItemResponseDTO()
        val firstTag = getTag()
        val secondTag = getTag()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(firstTag, secondTag)
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(2, result.tags.size)
        assertEquals(firstTag, result.tags[0])
        assertEquals(secondTag, result.tags[1])
    }

    @Test
    fun `toDomain should handle null assignee`() {
        val response = getWorkItemResponseDTO().copy(assignedToExtraInfo = null)

        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertNull(result.assignee)
    }

    @Test
    fun `toDomain should use assignedUsers when available`() {
        val user = getUser()
        val assignedUsers = listOf(getRandomLong(), getRandomLong(), getRandomLong())
        val response = getWorkItemResponseDTO().copy(assignedUsers = assignedUsers)

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(assignedUsers, result.assignedUserIds)
    }

    @Test
    fun `toDomain should fallback to assignedTo when assignedUsers is null`() {
        val user = getUser()
        val assignedTo = getRandomLong()
        val response = getWorkItemResponseDTO().copy(
            assignedUsers = null,
            assignedTo = assignedTo
        )

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(listOf(assignedTo), result.assignedUserIds)
    }

    @Test
    fun `toDomain should handle empty watchers`() {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(watchers = null)

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(emptyList(), result.watcherUserIds)
    }

    @Test
    fun `toDomain should handle null description`() {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(description = null)

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals("", result.description)
    }

    @Test
    fun `toDomain should map userStory when present`() {
        val user = getUser()
        val userStoryShortInfoDTO = getUserStoryShortInfoDTO()
        val userStoryShortInfo = UserStoryShortInfo(
            id = userStoryShortInfoDTO.id,
            ref = userStoryShortInfoDTO.ref,
            title = userStoryShortInfoDTO.title,
            epics = persistentListOf()
        )
        val response = getWorkItemResponseDTO().copy(userStoryExtraInfo = userStoryShortInfoDTO)

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()
        every { userStoryShortInfoMapper.toDomain(userStoryShortInfoDTO) } returns userStoryShortInfo

        val result = sut.toDomain(response)

        assertNotNull(result.userStory)
        assertEquals(userStoryShortInfo.id, result.userStory?.id)
        assertEquals(userStoryShortInfo.ref, result.userStory?.ref)
        assertEquals(userStoryShortInfo.title, result.userStory?.title)
    }

    @Test
    fun `toDomain should handle null userStory`() {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(userStoryExtraInfo = null)

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.DueSoon
        coEvery { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertNull(result.userStory)
    }

    @Test
    fun `toDomainList should map list of DTOs correctly`() {
        val user = getUser()
        val response1 = getWorkItemResponseDTO()
        val response2 = getWorkItemResponseDTO()
        val dtos = listOf(response1, response2)

        coEvery { userMapper.toUser(any()) } returns user

        dtos.forEach {
            every { dueDateStatusMapper.toDomain(it.dueDateStatusDTO) } returns DueDateStatus.DueSoon
            coEvery { projectMapper.toProjectExtraInfo(it.projectDTOExtraInfo) } returns getProjectExtraInfo()
            coEvery { tagsMapper.toTags(tags = it.tags) } returns persistentListOf(getTag())
            coEvery { statusesMapper.getStatus(resp = it) } returns getStatus()
        }

        val result = sut.toDomainList(dtos)

        assertEquals(2, result.size)
        assertEquals(response1.id, result[0].id)
        assertEquals(response2.id, result[1].id)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() {
        val result = sut.toDomainList(emptyList())

        assertEquals(0, result.size)
    }
}
