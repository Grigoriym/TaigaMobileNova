package com.grappim.taigamobile.feature.userstories.mapper

import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.testing.models.getEpicShortInfoDTO
import com.grappim.taigamobile.testing.models.getTag
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class UserStoryMapperTest {

    private val userMapper: UserMapper = UserMapper()
    private val statusesMapper: StatusesMapper = StatusesMapper()
    private val projectMapper: ProjectMapper = ProjectMapper()
    private val tagsMapper: TagsMapper = TagsMapper()
    private val dueDateStatusMapper: DueDateStatusMapper = DueDateStatusMapper()
    private val serverStorage: ServerStorage = FakeServerStorage()

    private lateinit var sut: UserStoryMapper

    @BeforeTest
    fun setup() {
        sut = UserStoryMapper(
            userMapper = userMapper,
            statusesMapper = statusesMapper,
            projectMapper = projectMapper,
            tagsMapper = tagsMapper,
            dueDateStatusMapper = dueDateStatusMapper,
            serverStorage = serverStorage
        )
    }

    @Test
    fun `toDomain should map basic fields correctly`() {
        val response = getWorkItemResponseDTO()

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
        assertEquals(dueDateStatusMapper.toDomain(response.dueDateStatusDTO), result.dueDateStatus)
        assertEquals(userMapper.toUser(response.assignedToExtraInfo!!), result.assignee)
    }

    @Test
    fun `toDomain should handle null owner with error`() {
        val response = getWorkItemResponseDTO().copy(owner = null)

        try {
            sut.toDomain(response)
            fail("Expected error for null owner")
        } catch (e: IllegalStateException) {
            assertEquals("Owner field is null", e.message)
        }
    }

    @Test
    fun `toDomain should map due date status correctly`() {
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = DueDateStatusDTO.DueSoon)

        val result = sut.toDomain(response)

        assertNotNull(result.dueDateStatus)
    }

    @Test
    fun `toDomain should handle blocked note correctly`() {
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )
        val result = sut.toDomain(response)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() {
        val response = getWorkItemResponseDTO()

        val result = sut.toDomain(response)

        val expectedUrl =
            "https://taiga.example.com/project/${response.projectDTOExtraInfo.slug}/us/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly`() {
        val response = getWorkItemResponseDTO()

        val firstTag = getTag()
        val secondTag = getTag()

        val result = sut.toDomain(response)

        assertEquals(2, result.tags.size)
    }

    @Test
    fun `toDomain should map null assignedToExtraInfo to null assignee`() {
        val response = getWorkItemResponseDTO().copy(assignedToExtraInfo = null)

        val result = sut.toDomain(response)

        assertNull(result.assignee)
    }

    @Test
    fun `toDomain should fall back to assignedTo when assignedUsers is null`() {
        val assignedTo = getRandomLong()
        val response = getWorkItemResponseDTO().copy(
            assignedUsers = null,
            assignedTo = assignedTo
        )

        val result = sut.toDomain(response)

        assertEquals(listOf(assignedTo), result.assignedUserIds)
    }

    @Test
    fun `toDomain should map empty assignedUserIds when assignedUsers and assignedTo are null`() {
        val response = getWorkItemResponseDTO().copy(
            assignedUsers = null,
            assignedTo = null
        )

        val result = sut.toDomain(response)

        assertEquals(emptyList(), result.assignedUserIds)
    }

    @Test
    fun `toDomain should map null watchers to empty watcherUserIds`() {
        val response = getWorkItemResponseDTO().copy(watchers = null)

        val result = sut.toDomain(response)

        assertEquals(emptyList(), result.watcherUserIds)
    }

    @Test
    fun `toDomain should map null description to empty string`() {
        val response = getWorkItemResponseDTO().copy(description = null)

        val result = sut.toDomain(response)

        assertEquals("", result.description)
    }

    @Test
    fun `toDomain should set wasPromotedFromTask when fromTaskRef is not empty`() {
        val response = getWorkItemResponseDTO().copy(fromTaskRef = getRandomString())

        val result = sut.toDomain(response)

        assertTrue(result.wasPromotedFromTask)
    }

    @Test
    fun `toDomain should not set wasPromotedFromTask when fromTaskRef is empty`() {
        val response = getWorkItemResponseDTO().copy(fromTaskRef = "")

        val result = sut.toDomain(response)

        assertFalse(result.wasPromotedFromTask)
    }

    @Test
    fun `toDomain should not set wasPromotedFromTask when fromTaskRef is null`() {
        val response = getWorkItemResponseDTO().copy(fromTaskRef = null)

        val result = sut.toDomain(response)

        assertFalse(result.wasPromotedFromTask)
    }

    @Test
    fun `toDomain should map epics to userStoryEpics`() {
        val first = getEpicShortInfoDTO()
        val second = getEpicShortInfoDTO()
        val response = getWorkItemResponseDTO().copy(epics = listOf(first, second))

        val result = sut.toDomain(response)

        assertEquals(2, result.userStoryEpics.size)
        assertEquals(
            UserStoryEpic(
                id = first.id,
                title = first.title,
                ref = first.ref,
                color = first.color
            ),
            result.userStoryEpics.first()
        )
        assertEquals(second.id, result.userStoryEpics.last().id)
    }

    @Test
    fun `toDomain should map null epics to empty userStoryEpics`() {
        val response = getWorkItemResponseDTO().copy(epics = null)

        val result = sut.toDomain(response)

        assertTrue(result.userStoryEpics.isEmpty())
    }

    @Test
    fun `toListDomain should map every item`() {
        val responses = listOf(getWorkItemResponseDTO(), getWorkItemResponseDTO())

        val result = sut.toListDomain(responses)

        assertEquals(responses.map { it.id }, result.map { it.id })
    }
}
