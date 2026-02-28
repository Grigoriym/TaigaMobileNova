package com.grappim.taigamobile.feature.sprint.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.sprint.domain.SprintData
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getSprint
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeSprintsRepository
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class SprintViewModelTest {

    private val sprintId = getRandomLong()
    private val savedStateHandle = SavedStateHandle(mapOf("sprintId" to sprintId))

    private val sprintsRepository = FakeSprintsRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val dateTimeUtils = FakeDateTimeUtils()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: SprintViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
        projectsRepository.permissions = persistentListOf()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = SprintViewModel(
            sprintsRepository = sprintsRepository,
            projectsRepository = projectsRepository,
            dateTimeUtils = dateTimeUtils,
            savedStateHandle = savedStateHandle
        )
    }

    private fun makeSprintData() = SprintData(
        sprint = getSprint(),
        statuses = persistentListOf(),
        storiesWithTasks = persistentMapOf(),
        issues = persistentListOf(),
        storylessTasks = persistentListOf()
    )

    // --- permissions ---

    @Test
    fun `on init - MODIFY_MILESTONE permission - canEdit is true`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.MODIFY_MILESTONE)

        createViewModel()

        assertTrue(sut.state.value.canEdit)
    }

    @Test
    fun `on init - no MODIFY_MILESTONE permission - canEdit is false`() {
        createViewModel()

        assertFalse(sut.state.value.canEdit)
    }

    @Test
    fun `on init - DELETE_MILESTONE permission - canDelete is true`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.DELETE_MILESTONE)

        createViewModel()

        assertTrue(sut.state.value.canDelete)
    }

    @Test
    fun `on init - MODIFY_MILESTONE only - canShowTopBarActions is true`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.MODIFY_MILESTONE)

        createViewModel()

        assertTrue(sut.state.value.canShowTopBarActions)
    }

    @Test
    fun `on init - no permissions - canShowTopBarActions is false`() {
        createViewModel()

        assertFalse(sut.state.value.canShowTopBarActions)
    }

    @Test
    fun `on init - ADD_ISSUE permission - canCreateIssue is true`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.ADD_ISSUE)

        createViewModel()

        assertTrue(sut.state.value.canCreateIssue)
    }

    @Test
    fun `on init - ADD_TASK permission - canCreateTasks is true`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.ADD_TASK)

        createViewModel()

        assertTrue(sut.state.value.canCreateTasks)
    }

    // --- load data ---

    @Test
    fun `on init - getSprintData success - state has sprint and isLoading false`() {
        val sprintData = makeSprintData()
        sprintsRepository.getSprintDataResult = Result.success(sprintData)

        createViewModel()

        with(sut.state.value) {
            assertEquals(sprintData.sprint, sprint)
            assertFalse(isLoading)
            assertEquals(NativeText.Empty, error)
        }
    }

    @Test
    fun `on init - getSprintData failure - isLoading false and error set`() = runTest {
        // getSprintDataResult defaults to failure

        createViewModel()

        sut.snackBarMessage.test {
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        with(sut.state.value) {
            assertFalse(isLoading)
            assertTrue(error !is NativeText.Empty)
        }
    }

    @Test
    fun `onRefresh - reloads sprint data`() {
        val first = makeSprintData()
        sprintsRepository.getSprintDataResult = Result.success(first)
        createViewModel()

        val second = makeSprintData()
        sprintsRepository.getSprintDataResult = Result.success(second)
        sut.state.value.onRefresh()

        assertEquals(second.sprint, sut.state.value.sprint)
    }

    // --- menu / delete dialog ---

    @Test
    fun `setIsMenuExpanded true - isMenuExpanded becomes true`() {
        createViewModel()

        sut.state.value.setIsMenuExpanded(true)

        assertTrue(sut.state.value.isMenuExpanded)
    }

    @Test
    fun `setIsMenuExpanded false - isMenuExpanded becomes false`() {
        createViewModel()
        sut.state.value.setIsMenuExpanded(true)

        sut.state.value.setIsMenuExpanded(false)

        assertFalse(sut.state.value.isMenuExpanded)
    }

    @Test
    fun `setIsDeleteDialogVisible true - isDeleteDialogVisible becomes true`() {
        createViewModel()

        sut.state.value.setIsDeleteDialogVisible(true)

        assertTrue(sut.state.value.isDeleteDialogVisible)
    }

    // --- delete sprint ---

    @Test
    fun `onDeleteSprint success - emits deleteResult and isLoading false`() = runTest {
        createViewModel()

        sut.deleteResult.test {
            sut.state.value.onDeleteSprint()
            awaitItem()
        }

        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `onDeleteSprint failure - error set and deleteResult not emitted`() = runTest {
        sprintsRepository.deleteSprintThrows = testException
        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onDeleteSprint()
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        with(sut.state.value) {
            assertFalse(isLoading)
            assertTrue(error !is NativeText.Empty)
        }
    }

    // --- edit sprint ---

    @Test
    fun `onEditSprintClick - sprint dialog becomes visible`() {
        sprintsRepository.getSprintDataResult = Result.success(makeSprintData())
        createViewModel()

        sut.state.value.onEditSprintClick()

        assertTrue(sut.sprintDialogState.value.isSprintDialogVisible)
    }

    @Test
    fun `onEditSprintClick - initializes sprint dates from loaded sprint`() {
        sprintsRepository.getSprintDataResult = Result.success(makeSprintData())
        createViewModel()

        sut.state.value.onEditSprintClick()

        with(sut.sprintDialogState.value) {
            assertNotNull(startDate)
            assertNotNull(endDate)
        }
    }

    @Test
    fun `onEditSprintConfirm success - isLoading false after completion`() {
        sprintsRepository.getSprintDataResult = Result.success(makeSprintData())
        createViewModel()
        sut.state.value.onEditSprintClick()
        sut.sprintDialogState.value.onSetSprintNameValue("Sprint 1")

        sut.state.value.onEditSprintConfirm()

        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `onEditSprintConfirm with empty sprint name - sets sprintNameError`() {
        sprintsRepository.getSprintDataResult = Result.success(makeSprintData())
        createViewModel()
        sut.state.value.onEditSprintClick()
        sut.sprintDialogState.value.onSetSprintNameValue("") // clear the name set by onEditSprintClick

        sut.state.value.onEditSprintConfirm()

        assertTrue(sut.sprintDialogState.value.sprintNameError !is NativeText.Empty)
    }
}
