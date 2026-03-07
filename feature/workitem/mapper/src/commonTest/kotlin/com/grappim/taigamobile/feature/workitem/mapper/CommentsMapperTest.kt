package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.dto.CommentDTO
import com.grappim.taigamobile.testing.models.getUserDTO
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDateTime
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommentsMapperTest {

    private val userMapper: UserMapper = UserMapper()
    private val projectsRepository: FakeProjectsRepository = FakeProjectsRepository()

    private lateinit var sut: CommentsMapper

    @BeforeTest
    fun setup() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.MODIFY_PROJECT)
        sut = CommentsMapper(
            userMapper = userMapper,
            projectsRepository = projectsRepository
        )
    }

    @Test
    fun `toDomain should map basic fields correctly`() = runTest {
        val userDTO = getUserDTO()
        val user = userMapper.toUser(userDTO)
        val postDateTime = nowLocalDateTime
        val deleteDate = nowLocalDateTime
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
        val currentUserId = getRandomLong()

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = nowLocalDateTime,
            deleteDate = null
        )

        val result = sut.toDomain(dto, currentUserId)

        assertNull(result.deleteDate)
    }

    @Test
    fun `toDomain should set canDelete true when user is author and has modify permission`() = runTest {
        val authorId = getRandomLong()
        val userDTO = getUserDTO().copy(id = authorId, pk = null)

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = nowLocalDateTime,
            deleteDate = null
        )
        val result = sut.toDomain(dto, authorId)

        assertTrue(result.canDelete)
    }

    @Test
    fun `toDomain should set canDelete false when user is author but lacks modify permission`() = runTest {
        val authorId = getRandomLong()
        val userDTO = getUserDTO().copy(id = authorId, pk = null)

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = nowLocalDateTime,
            deleteDate = null
        )

        projectsRepository.permissions = persistentListOf()
        val result = sut.toDomain(dto, authorId)

        assertFalse(result.canDelete)
    }

    @Test
    fun `toDomain should set canDelete false when user is not author but has modify permission`() = runTest {
        val authorId = getRandomLong()
        val differentUserId = authorId + 1
        val userDTO = getUserDTO().copy(id = authorId, pk = null)

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = nowLocalDateTime,
            deleteDate = null
        )

        val result = sut.toDomain(dto, differentUserId)

        assertFalse(result.canDelete)
    }

    @Test
    fun `toDomain should set canDelete false when user is not author and lacks permission`() = runTest {
        val authorId = getRandomLong()
        val differentUserId = authorId + 1
        val userDTO = getUserDTO().copy(id = authorId, pk = null)

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = nowLocalDateTime,
            deleteDate = null
        )

        val result = sut.toDomain(dto, differentUserId)

        assertFalse(result.canDelete)
    }

    @Test
    fun `toDomain should use pk as actualId when id is null`() = runTest {
        val authorPk = getRandomLong()
        val userDTO = getUserDTO().copy(id = null, pk = authorPk)

        val dto = CommentDTO(
            id = getRandomString(),
            author = userDTO,
            text = getRandomString(),
            postDateTime = nowLocalDateTime,
            deleteDate = null
        )

        val result = sut.toDomain(dto, authorPk)

        assertTrue(result.canDelete)
    }
}
