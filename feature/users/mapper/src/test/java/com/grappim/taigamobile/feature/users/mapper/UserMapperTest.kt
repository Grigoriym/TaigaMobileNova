package com.grappim.taigamobile.feature.users.mapper

import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getUserDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class UserMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sut: UserMapper

    @Before
    fun setup() {
        sut = UserMapper(ioDispatcher = testDispatcher)
    }

    @Test
    fun `toUser should map all fields correctly`() = runTest {
        val dto = getUserDTO()

        val result = sut.toUser(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.fullName, result.fullName)
        assertEquals(dto.photo, result.photo)
        assertEquals(dto.bigPhoto, result.bigPhoto)
        assertEquals(dto.username, result.username)
        assertEquals(dto.name, result.name)
        assertEquals(dto.pk, result.pk)
    }

    @Test
    fun `toUser should handle null id`() = runTest {
        val dto = getUserDTO().copy(id = null)

        val result = sut.toUser(dto)

        assertNull(result.id)
    }

    @Test
    fun `toUser should handle null fullName`() = runTest {
        val dto = getUserDTO().copy(fullName = null)

        val result = sut.toUser(dto)

        assertNull(result.fullName)
    }

    @Test
    fun `toUser should handle null photo`() = runTest {
        val dto = getUserDTO().copy(photo = null)

        val result = sut.toUser(dto)

        assertNull(result.photo)
    }

    @Test
    fun `toUser should handle null bigPhoto`() = runTest {
        val dto = getUserDTO().copy(bigPhoto = null)

        val result = sut.toUser(dto)

        assertNull(result.bigPhoto)
    }

    @Test
    fun `toUser should handle null name`() = runTest {
        val dto = getUserDTO().copy(name = null)

        val result = sut.toUser(dto)

        assertNull(result.name)
    }

    @Test
    fun `toUser should handle null pk`() = runTest {
        val dto = getUserDTO().copy(pk = null)

        val result = sut.toUser(dto)

        assertNull(result.pk)
    }

    @Test
    fun `toUser should preserve username`() = runTest {
        val username = getRandomString()
        val dto = getUserDTO().copy(username = username)

        val result = sut.toUser(dto)

        assertEquals(username, result.username)
    }
}
