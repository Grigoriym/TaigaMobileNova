package com.grappim.taigamobile.feature.users.mapper

import com.grappim.taigamobile.feature.projects.dto.ProjectMemberDTO
import com.grappim.taigamobile.testing.getRandomInt
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TeamMemberMapperTest {

    private lateinit var sut: TeamMemberMapper

    @Before
    fun setup() {
        sut = TeamMemberMapper()
    }

    private fun getProjectMemberDTO(
        id: Long = getRandomLong(),
        photo: String? = getRandomString(),
        fullNameDisplay: String = getRandomString(),
        roleName: String = getRandomString(),
        username: String = getRandomString()
    ) = ProjectMemberDTO(
        id = id,
        photo = photo,
        fullNameDisplay = fullNameDisplay,
        roleName = roleName,
        username = username
    )

    @Test
    fun `toDomain should map all fields correctly`() {
        val dto = getProjectMemberDTO()
        val totalPower = getRandomInt()
        val stats = mapOf(dto.id to totalPower)

        val result = sut.toDomain(listOf(dto), stats)

        assertEquals(1, result.size)
        assertEquals(dto.id, result[0].id)
        assertEquals(dto.photo, result[0].avatarUrl)
        assertEquals(dto.fullNameDisplay, result[0].name)
        assertEquals(dto.roleName, result[0].role)
        assertEquals(dto.username, result[0].username)
        assertEquals(totalPower, result[0].totalPower)
    }

    @Test
    fun `toDomain should handle null photo`() {
        val dto = getProjectMemberDTO(photo = null)
        val stats = mapOf(dto.id to getRandomInt())

        val result = sut.toDomain(listOf(dto), stats)

        assertNull(result[0].avatarUrl)
    }

    @Test
    fun `toDomain should handle missing stats for member`() {
        val dto = getProjectMemberDTO()
        val stats = emptyMap<Long, Int>()

        val result = sut.toDomain(listOf(dto), stats)

        assertNull(result[0].totalPower)
    }

    @Test
    fun `toDomain should handle empty list`() {
        val result = sut.toDomain(emptyList(), emptyMap())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toDomain should map multiple members correctly`() {
        val dto1 = getProjectMemberDTO()
        val dto2 = getProjectMemberDTO()
        val dto3 = getProjectMemberDTO()
        val power1 = getRandomInt()
        val power2 = getRandomInt()
        val stats = mapOf(
            dto1.id to power1,
            dto2.id to power2
        )

        val result = sut.toDomain(listOf(dto1, dto2, dto3), stats)

        assertEquals(3, result.size)
        assertEquals(power1, result[0].totalPower)
        assertEquals(power2, result[1].totalPower)
        assertNull(result[2].totalPower)
    }

    @Test
    fun `toDomain should return immutable list`() {
        val dto = getProjectMemberDTO()
        val stats = mapOf(dto.id to getRandomInt())

        val result = sut.toDomain(listOf(dto), stats)

        assertEquals(1, result.size)
    }
}
