package com.grappim.taigamobile.feature.epics.domain

import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.testing.FakePatchDataGenerator
import com.grappim.taigamobile.testing.models.getCustomFields
import com.grappim.taigamobile.testing.models.getEpic
import com.grappim.taigamobile.testing.models.getFiltersData
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeEpicsRepository
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EpicDetailsDataUseCaseImplTest {

    private val fakeFiltersRepository = FakeFiltersRepository()
    private val fakeEpicsRepository = FakeEpicsRepository()
    private val fakeHistoryRepository = FakeHistoryRepository()
    private val fakeWorkItemRepository = FakeWorkItemRepository()
    private val fakeUsersRepository = FakeUsersRepository()
    private val fakeUserStoriesRepository = FakeUserStoriesRepository()
    private val fakePatchDataGenerator = FakePatchDataGenerator()
    private val fakeProjectsRepository = FakeProjectsRepository()

    private val useCase = EpicDetailsDataUseCaseImpl(
        filtersRepository = fakeFiltersRepository,
        epicsRepository = fakeEpicsRepository,
        historyRepository = fakeHistoryRepository,
        workItemRepository = fakeWorkItemRepository,
        usersRepository = fakeUsersRepository,
        userStoriesRepository = fakeUserStoriesRepository,
        patchDataGenerator = fakePatchDataGenerator,
        projectsRepository = fakeProjectsRepository
    )

    private fun setupGetEpicDataDefaults() {
        val epic = getEpic()
        fakeFiltersRepository.filtersDataResult = getFiltersData()
        fakeEpicsRepository.getEpicResult = epic
        fakeWorkItemRepository.getWorkItemAttachmentsResult = persistentListOf()
        fakeWorkItemRepository.getCustomFieldsResult = getCustomFields()
        fakeHistoryRepository.getCommentsResult = persistentListOf()
        fakeUserStoriesRepository.getEpicUserStoriesSimplifiedResult = persistentListOf()
        fakeUsersRepository.getUserResult = getUser()
        fakeUsersRepository.getUsersListResult = persistentListOf()
        fakeUsersRepository.isAnyAssignedToMeResult = false
    }

    @Test
    fun `getEpicData returns success with assembled data`() = runTest {
        val epic = getEpic()
        val filtersData = getFiltersData()
        val customFields = getCustomFields()

        fakeFiltersRepository.filtersDataResult = filtersData
        fakeEpicsRepository.getEpicResult = epic
        fakeWorkItemRepository.getWorkItemAttachmentsResult = persistentListOf()
        fakeWorkItemRepository.getCustomFieldsResult = customFields
        fakeHistoryRepository.getCommentsResult = persistentListOf()
        fakeUserStoriesRepository.getEpicUserStoriesSimplifiedResult = persistentListOf()
        fakeUsersRepository.getUserResult = getUser()
        fakeUsersRepository.getUsersListResult = persistentListOf()
        fakeUsersRepository.isAnyAssignedToMeResult = false

        val result = useCase.getEpicData(epic.id)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(epic, data.epic)
        assertEquals(filtersData, data.filtersData)
        assertEquals(customFields, data.customFields)
    }

    @Test
    fun `getEpicData returns failure when epicsRepository throws`() = runTest {
        setupGetEpicDataDefaults()
        fakeEpicsRepository.getEpicThrows = RuntimeException("network error")

        val result = useCase.getEpicData(1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getEpicData maps permission flags from projectsRepository`() = runTest {
        setupGetEpicDataDefaults()
        fakeProjectsRepository.permissions = persistentListOf(
            TaigaPermission.MODIFY_EPIC,
            TaigaPermission.COMMENT_EPIC
        )

        val result = useCase.getEpicData(1L)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertTrue(data.canModifyEpic)
        assertTrue(data.canComment)
        assertFalse(data.canDeleteEpic)
    }

    @Test
    fun `changeEpicColor returns success with patched data and user stories`() = runTest {
        val patchedData = PatchedData(newVersion = 2L, dueDateStatus = null)
        val userStories = persistentListOf(getWorkItem())
        fakeWorkItemRepository.patchDataResult = patchedData
        fakeUserStoriesRepository.getEpicUserStoriesSimplifiedResult = userStories

        val result = useCase.changeEpicColor(color = "#FF0000", version = 1L, epicId = 42L)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(patchedData, data.patchedData)
        assertEquals(userStories, data.userStories)
    }

    @Test
    fun `changeEpicColor returns failure when patchData throws`() = runTest {
        fakeWorkItemRepository.patchDataThrows = RuntimeException("patch failed")

        val result = useCase.changeEpicColor(color = "#FF0000", version = 1L, epicId = 42L)

        assertTrue(result.isFailure)
    }
}
