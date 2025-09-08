package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.core.api.UserMapper
import com.grappim.taigamobile.core.domain.DueDateStatusDTO
import com.grappim.taigamobile.feature.filters.domain.model.Severity
import com.grappim.taigamobile.testing.getCommonTaskResponse
import com.grappim.taigamobile.testing.getFiltersData
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getUser
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

    private lateinit var sut: IssueTaskMapper

    @Before
    fun setup() {
        sut = IssueTaskMapper(
            ioDispatcher = testDispatcher,
            userMapper = userMapper
        )
    }

    @Test
    fun `toDomain should map basic fields correctly`() = runTest {
        val server = "https://taiga.example.com"
        val response = getCommonTaskResponse()
        val filtersData = getFiltersData(
            newStatuses = persistentListOf(
                Severity(
                    id = response.status,
                    name = getRandomString(),
                    color = getRandomString(),
                    count = getRandomLong(),
                    order = getRandomLong()
                )
            )
        )
        val user = getUser()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user

        val result = sut.toDomain(response, server, filtersData)

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
        assertEquals(user, result.assignee)
    }

    @Test
    fun `toDomain should handle null owner with error`() = runTest {
        val server = "https://taiga.example.com"
        val response = getCommonTaskResponse().copy(owner = null)
        val filtersData = getFiltersData()

        try {
            sut.toDomain(response, server, filtersData)
            assert(false) { "Expected error for null owner" }
        } catch (e: IllegalStateException) {
            assertEquals("Owner field is null", e.message)
        }
    }

    @Test
    fun `toDomain should map due date status correctly`() = runTest {
        val user = getUser()
        val server = "https://taiga.example.com"
        val response = getCommonTaskResponse().copy(dueDateStatusDTO = DueDateStatusDTO.DueSoon)
        val filtersData = getFiltersData()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user

        val result = sut.toDomain(response, server, filtersData)

        assertNotNull(result.dueDateStatus)
    }

    @Test
    fun `toDomain should handle blocked note correctly`() = runTest {
        val user = getUser()
        val server = "https://taiga.example.com"
        val blockedNote = getRandomString()
        val response = getCommonTaskResponse().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )
        val filtersData = getFiltersData()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user

        val result = sut.toDomain(response, server, filtersData)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() = runTest {
        val user = getUser()
        val server = "https://taiga.example.com"
        val response = getCommonTaskResponse()
        val filtersData = getFiltersData()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user

        val result = sut.toDomain(response, server, filtersData)

        val expectedUrl =
            "$server/project/${response.projectDTOExtraInfo.slug}/issue/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly from nested list`() = runTest {
        val user = getUser()
        val server = "https://taiga.example.com"
        val tagName1 = getRandomString()
        val tagColor1 = "#FF5722"
        val tagName2 = getRandomString()
        val tagColor2 = "#2196F3"

        val response = getCommonTaskResponse().copy(
            tags = listOf(
                listOf(tagName1, tagColor1, "5"),
                listOf(tagName2, tagColor2, "3")
            )
        )
        val filtersData = getFiltersData()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user

        val result = sut.toDomain(response, server, filtersData)

        assertEquals(2, result.tags.size)
        assertEquals(tagName1, result.tags[0].name)
        assertEquals(tagColor1, result.tags[0].color)
        assertEquals(5L, result.tags[0].count)
        assertEquals(tagName2, result.tags[1].name)
        assertEquals(tagColor2, result.tags[1].color)
        assertEquals(3L, result.tags[1].count)
    }
}
