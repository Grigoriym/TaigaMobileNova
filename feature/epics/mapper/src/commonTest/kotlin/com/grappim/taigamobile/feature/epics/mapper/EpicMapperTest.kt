package com.grappim.taigamobile.feature.epics.mapper

import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class EpicMapperTest {

    private val userMapper: UserMapper = UserMapper()
    private val statusesMapper: StatusesMapper = StatusesMapper()
    private val projectMapper: ProjectMapper = ProjectMapper()
    private val tagsMapper: TagsMapper = TagsMapper()
    private val serverStorage: ServerStorage = FakeServerStorage()

    private lateinit var sut: EpicMapper

    @BeforeTest
    fun setup() {
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
    }

    @Test
    fun `toDomain should handle null owner with error`() = runTest {
        val response = getWorkItemResponseDTO().copy(owner = null)

        try {
            sut.toDomain(response)
            fail("Expected error for null owner")
        } catch (e: IllegalStateException) {
            assertEquals("Owner field is null", e.message)
        }
    }

    @Test
    fun `toDomain should handle blocked note correctly when blocked`() = runTest {
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )

        val result = sut.toDomain(response)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should return null blockedNote when not blocked`() = runTest {
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = false,
            blockedNote = blockedNote
        )

        val result = sut.toDomain(response)

        assertNull(result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() = runTest {
        val response = getWorkItemResponseDTO()

        val result = sut.toDomain(response)

        val expectedUrl =
            "https://taiga.example.com/project/${response.projectDTOExtraInfo.slug}/epic/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly`() = runTest {
        val response = getWorkItemResponseDTO()

        val result = sut.toDomain(response)

        assertEquals(2, result.tags.size)
    }

    @Test
    fun `toDomain should handle null assignee`() = runTest {
        val response = getWorkItemResponseDTO().copy(assignedToExtraInfo = null)

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
        val result = sut.toDomain(response)

        assertEquals(listOf(assignedToId), result.assignedUserIds)
    }

    @Test
    fun `toDomain should handle null description`() = runTest {
        val response = getWorkItemResponseDTO().copy(description = null)

        val result = sut.toDomain(response)

        assertEquals("", result.description)
    }

    @Test
    fun `toDomain should handle null watchers`() = runTest {
        val response = getWorkItemResponseDTO().copy(watchers = null)
        val result = sut.toDomain(response)

        assertEquals(emptyList(), result.watcherUserIds)
    }

    @Test
    fun `toDomainList should map list of DTOs`() = runTest {
        val response1 = getWorkItemResponseDTO()
        val response2 = getWorkItemResponseDTO()

        val result = sut.toDomainList(listOf(response1, response2))

        assertEquals(2, result.size)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() = runTest {
        val result = sut.toDomainList(emptyList())

        assertEquals(0, result.size)
    }
}
