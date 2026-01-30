package com.grappim.taigamobile.feature.users.mapper

import com.grappim.taigamobile.feature.users.dto.StatsDTO
import com.grappim.taigamobile.testing.getRandomInt
import com.grappim.taigamobile.testing.getRandomString
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserStatsMapperTest {

    private lateinit var sut: UserStatsMapper

    @Before
    fun setup() {
        sut = UserStatsMapper()
    }

    @Test
    fun `toDomain should map all fields correctly`() {
        val dto = StatsDTO(
            roles = listOf(getRandomString(), getRandomString(), getRandomString()),
            totalNumClosedUserStories = getRandomInt(),
            totalNumContacts = getRandomInt(),
            totalNumProjects = getRandomInt()
        )

        val result = sut.toDomain(dto)

        assertEquals(dto.roles, result.roles)
        assertEquals(dto.totalNumClosedUserStories, result.totalNumClosedUserStories)
        assertEquals(dto.totalNumContacts, result.totalNumContacts)
        assertEquals(dto.totalNumProjects, result.totalNumProjects)
    }

    @Test
    fun `toDomain should handle empty roles list`() {
        val dto = StatsDTO(
            roles = emptyList(),
            totalNumClosedUserStories = getRandomInt(),
            totalNumContacts = getRandomInt(),
            totalNumProjects = getRandomInt()
        )

        val result = sut.toDomain(dto)

        assertTrue(result.roles.isEmpty())
    }

    @Test
    fun `toDomain should convert roles to immutable list`() {
        val roles = listOf(getRandomString(), getRandomString())
        val dto = StatsDTO(
            roles = roles,
            totalNumClosedUserStories = getRandomInt(),
            totalNumContacts = getRandomInt(),
            totalNumProjects = getRandomInt()
        )

        val result = sut.toDomain(dto)

        assertEquals(roles.size, result.roles.size)
        assertEquals(roles[0], result.roles[0])
        assertEquals(roles[1], result.roles[1])
    }

    @Test
    fun `toDomain should handle zero values`() {
        val dto = StatsDTO(
            roles = emptyList(),
            totalNumClosedUserStories = 0,
            totalNumContacts = 0,
            totalNumProjects = 0
        )

        val result = sut.toDomain(dto)

        assertEquals(0, result.totalNumClosedUserStories)
        assertEquals(0, result.totalNumContacts)
        assertEquals(0, result.totalNumProjects)
    }
}
