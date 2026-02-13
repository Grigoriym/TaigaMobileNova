package com.grappim.taigamobile.feature.epics.mapper

import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.testing.getProjectExtraInfo
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getStatus
import com.grappim.taigamobile.testing.getTag
import com.grappim.taigamobile.testing.getUser
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EpicMapperTest {

    private val userMapper: UserMapper = mockk()
    private val statusesMapper: StatusesMapper = mockk()
    private val projectMapper: ProjectMapper = mockk()
    private val tagsMapper: TagsMapper = mockk()
    private val serverStorage: ServerStorage = mockk()

    private lateinit var sut: EpicMapper

    @Before
    fun setup() {
        every { serverStorage.server } returns "https://taiga.example.com"

        sut = EpicMapper(
            serverStorage = serverStorage,
            statusesMapper = statusesMapper,
            userMapper = userMapper,
            projectMapper = projectMapper,
            tagsMapper = tagsMapper
        )
    }

    @Test
    fun `toDomain should map basic fields correctly`() = runTest {
        val response = getWorkItemResponseDTO()
        val user = getUser()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(response.id, result.id)
        assertEquals(response.version, result.version)
        assertEquals(response.createdDate, result.createdDateTime)
        assertEquals(response.owner, result.creatorId)
        assertEquals(response.subject, result.title)
        assertEquals(response.description, result.description)
        assertEquals(response.ref, result.ref)
        assertEquals(response.isClosed, result.isClosed)
        assertEquals(response.milestone, result.milestone)
        assertEquals(response.color, result.epicColor)
        assertEquals(user, result.assignee)
    }

    @Test
    fun `toDomain should handle null owner with error`() = runTest {
        val response = getWorkItemResponseDTO().copy(owner = null)

        try {
            sut.toDomain(response)
            assert(false) { "Expected error for null owner" }
        } catch (e: IllegalStateException) {
            assertEquals("Owner field is null", e.message)
        }
    }

    @Test
    fun `toDomain should handle blocked note correctly when blocked`() = runTest {
        val user = getUser()
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should return null blockedNote when not blocked`() = runTest {
        val user = getUser()
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = false,
            blockedNote = blockedNote
        )

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertNull(result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() = runTest {
        val user = getUser()
        val response = getWorkItemResponseDTO()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        val expectedUrl =
            "https://taiga.example.com/project/${response.projectDTOExtraInfo.slug}/epic/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly`() = runTest {
        val user = getUser()
        val response = getWorkItemResponseDTO()

        val firstTag = getTag()
        val secondTag = getTag()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(firstTag, secondTag)
        every { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(2, result.tags.size)
    }

    @Test
    fun `toDomain should handle null assignee`() = runTest {
        val response = getWorkItemResponseDTO().copy(assignedToExtraInfo = null)

        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertNull(result.assignee)
    }

    @Test
    fun `toDomain should use assignedTo when assignedUsers is null`() = runTest {
        val assignedToId = 123L
        val response = getWorkItemResponseDTO().copy(
            assignedUsers = null,
            assignedTo = assignedToId
        )
        val user = getUser()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(listOf(assignedToId), result.assignedUserIds)
    }

    @Test
    fun `toDomain should handle null description`() = runTest {
        val response = getWorkItemResponseDTO().copy(description = null)
        val user = getUser()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals("", result.description)
    }

    @Test
    fun `toDomain should handle null watchers`() = runTest {
        val response = getWorkItemResponseDTO().copy(watchers = null)
        val user = getUser()

        every { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        every { projectMapper.toProjectExtraInfo(response.projectDTOExtraInfo) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(emptyList(), result.watcherUserIds)
    }

    @Test
    fun `toDomainList should map list of DTOs`() = runTest {
        val response1 = getWorkItemResponseDTO()
        val response2 = getWorkItemResponseDTO()
        val user = getUser()

        every { userMapper.toUser(any()) } returns user
        every { projectMapper.toProjectExtraInfo(any()) } returns getProjectExtraInfo()
        every { tagsMapper.toTags(tags = any()) } returns persistentListOf(getTag())
        every { statusesMapper.getStatus(any()) } returns getStatus()

        val result = sut.toDomainList(listOf(response1, response2))

        assertEquals(2, result.size)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() = runTest {
        val result = sut.toDomainList(emptyList())

        assertEquals(0, result.size)
    }
}
