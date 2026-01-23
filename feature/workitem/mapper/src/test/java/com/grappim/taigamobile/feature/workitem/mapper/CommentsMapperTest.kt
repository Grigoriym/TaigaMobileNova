package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.dto.CommentDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getUser
import com.grappim.taigamobile.testing.getUserDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CommentsMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val userMapper: UserMapper = mockk()
    private val projectsRepository: ProjectsRepository = mockk()

    private lateinit var sut: CommentsMapper

    @Before
    fun setup() {
        sut = CommentsMapper(
            ioDispatcher = testDispatcher,
            userMapper = userMapper,
            projectsRepository = projectsRepository
        )
    }

    @Test
    fun `toDomain should map basic fields correctly`() = runTest {
        val userDTO = getUserDTO()
        val user = getUser()
        val postDateTime = LocalDateTime.now()
        val deleteDate = LocalDateTime.now().plusDays(1)
        val commentId = getRandomString()
        val commentText = getRandomString()
        val currentUserId = getRandomLong()

        val dto = CommentDTO(
            id = commentId,
            author = userDTO,
            text = commentText,
            postDateTime = postDateTime,
            deleteDate = deleteDate
        )

        coEvery { userMapper.toUser(userDTO) } returns user
        coEvery { projectsRepository.getPermissions() } returns persistentListOf()

        val result = sut.toDomain(dto, currentUserId)

        assertEquals(commentId, result.id)
        assertEquals(user, result.author)
        assertEquals(commentText, result.text)
        assertEquals(postDateTime, result.postDateTime)
        assertEquals(deleteDate, result.deleteDate)
    }

    @Test
    fun `toDomain should handle null deleteDate`() = runTest {
        val userDTO = getUserDTO()
        val user = getUser()
        val currentUserId = getRandomLong()

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = LocalDateTime.now(),
            deleteDate = null
        )

        coEvery { userMapper.toUser(userDTO) } returns user
        coEvery { projectsRepository.getPermissions() } returns persistentListOf()

        val result = sut.toDomain(dto, currentUserId)

        assertNull(result.deleteDate)
    }

    @Test
    fun `toDomain should set canDelete true when user is author and has modify permission`() = runTest {
        val authorId = getRandomLong()
        val userDTO = getUserDTO().copy(id = authorId, pk = null)
        val user = getUser()

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = LocalDateTime.now(),
            deleteDate = null
        )

        coEvery { userMapper.toUser(userDTO) } returns user
        coEvery { projectsRepository.getPermissions() } returns persistentListOf(TaigaPermission.MODIFY_PROJECT)

        val result = sut.toDomain(dto, authorId)

        assertTrue(result.canDelete)
    }

    @Test
    fun `toDomain should set canDelete false when user is author but lacks modify permission`() = runTest {
        val authorId = getRandomLong()
        val userDTO = getUserDTO().copy(id = authorId, pk = null)
        val user = getUser()

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = LocalDateTime.now(),
            deleteDate = null
        )

        coEvery { userMapper.toUser(userDTO) } returns user
        coEvery { projectsRepository.getPermissions() } returns persistentListOf()

        val result = sut.toDomain(dto, authorId)

        assertFalse(result.canDelete)
    }

    @Test
    fun `toDomain should set canDelete false when user is not author but has modify permission`() = runTest {
        val authorId = getRandomLong()
        val differentUserId = authorId + 1
        val userDTO = getUserDTO().copy(id = authorId, pk = null)
        val user = getUser()

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = LocalDateTime.now(),
            deleteDate = null
        )

        coEvery { userMapper.toUser(userDTO) } returns user
        coEvery { projectsRepository.getPermissions() } returns persistentListOf(TaigaPermission.MODIFY_PROJECT)

        val result = sut.toDomain(dto, differentUserId)

        assertFalse(result.canDelete)
    }

    @Test
    fun `toDomain should set canDelete false when user is not author and lacks permission`() = runTest {
        val authorId = getRandomLong()
        val differentUserId = authorId + 1
        val userDTO = getUserDTO().copy(id = authorId, pk = null)
        val user = getUser()

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = LocalDateTime.now(),
            deleteDate = null
        )

        coEvery { userMapper.toUser(userDTO) } returns user
        coEvery { projectsRepository.getPermissions() } returns persistentListOf()

        val result = sut.toDomain(dto, differentUserId)

        assertFalse(result.canDelete)
    }

    @Test
    fun `toDomain should use pk as actualId when id is null`() = runTest {
        val authorPk = getRandomLong()
        val userDTO = getUserDTO().copy(id = null, pk = authorPk)
        val user = getUser()

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = LocalDateTime.now(),
            deleteDate = null
        )

        coEvery { userMapper.toUser(userDTO) } returns user
        coEvery { projectsRepository.getPermissions() } returns persistentListOf(TaigaPermission.MODIFY_PROJECT)

        val result = sut.toDomain(dto, authorPk)

        assertTrue(result.canDelete)
    }
}
