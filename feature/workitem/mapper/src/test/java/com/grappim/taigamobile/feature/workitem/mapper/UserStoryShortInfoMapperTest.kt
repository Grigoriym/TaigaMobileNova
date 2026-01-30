package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.epics.dto.EpicShortInfoDTO
import com.grappim.taigamobile.feature.userstories.dto.UserStoryShortInfoDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserStoryShortInfoMapperTest {

    private lateinit var sut: UserStoryShortInfoMapper

    @Before
    fun setup() {
        sut = UserStoryShortInfoMapper()
    }

    @Test
    fun `toDomain should map basic fields correctly`() = runTest {
        val dto = createUserStoryShortInfoDTO()

        val result = sut.toDomain(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.ref, result.ref)
        assertEquals(dto.title, result.title)
    }

    @Test
    fun `toDomain should map epics correctly`() = runTest {
        val epic1 = createEpicShortInfoDTO()
        val epic2 = createEpicShortInfoDTO()
        val dto = createUserStoryShortInfoDTO(epics = listOf(epic1, epic2))

        val result = sut.toDomain(dto)

        assertEquals(2, result.epics.size)
        assertEquals(epic1.id, result.epics[0].id)
        assertEquals(epic1.title, result.epics[0].title)
        assertEquals(epic1.ref, result.epics[0].ref)
        assertEquals(epic1.color, result.epics[0].color)
        assertEquals(epic2.id, result.epics[1].id)
        assertEquals(epic2.title, result.epics[1].title)
        assertEquals(epic2.ref, result.epics[1].ref)
        assertEquals(epic2.color, result.epics[1].color)
    }

    @Test
    fun `toDomain should handle null epics`() = runTest {
        val dto = createUserStoryShortInfoDTO(epics = null)

        val result = sut.toDomain(dto)

        assertTrue(result.epics.isEmpty())
    }

    @Test
    fun `toDomain should handle empty epics list`() = runTest {
        val dto = createUserStoryShortInfoDTO(epics = emptyList())

        val result = sut.toDomain(dto)

        assertTrue(result.epics.isEmpty())
    }

    private fun createUserStoryShortInfoDTO(
        id: Long = getRandomLong(),
        ref: Long = getRandomLong(),
        title: String = getRandomString(),
        epics: List<EpicShortInfoDTO>? = null
    ) = UserStoryShortInfoDTO(
        id = id,
        ref = ref,
        title = title,
        epics = epics
    )

    private fun createEpicShortInfoDTO(
        id: Long = getRandomLong(),
        title: String = getRandomString(),
        ref: Long = getRandomLong(),
        color: String = "#${getRandomString().take(6)}"
    ) = EpicShortInfoDTO(
        id = id,
        title = title,
        ref = ref,
        color = color
    )
}
