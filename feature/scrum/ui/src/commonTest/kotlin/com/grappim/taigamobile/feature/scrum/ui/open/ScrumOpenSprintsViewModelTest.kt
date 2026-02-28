package com.grappim.taigamobile.feature.scrum.ui.open

import app.cash.turbine.test
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeSprintsRepository
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class ScrumOpenSprintsViewModelTest {

    private val sprintsRepository = FakeSprintsRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val dateTimeUtils = FakeDateTimeUtils()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: ScrumOpenSprintsViewModel

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
        sut = ScrumOpenSprintsViewModel(
            sprintsRepository = sprintsRepository,
            projectsRepository = projectsRepository,
            dateTimeUtils = dateTimeUtils
        )
    }

    // --- permissions ---

    @Test
    fun `on init - ADD_MILESTONE permission present - canAddSprint is true`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.ADD_MILESTONE)

        createViewModel()

        assertTrue(sut.state.value.canAddSprint)
    }

    @Test
    fun `on init - no ADD_MILESTONE permission - canAddSprint is false`() {
        createViewModel()

        assertFalse(sut.state.value.canAddSprint)
    }

    // --- sprint dialog ---

    @Test
    fun `onCreateSprintClick - sprint dialog becomes visible`() {
        createViewModel()

        sut.state.value.onCreateSprintClick()

        assertTrue(sut.sprintDialogState.value.isSprintDialogVisible)
    }

    @Test
    fun `onCreateSprintClick - initializes sprint dates from dateTimeUtils`() {
        createViewModel()

        sut.state.value.onCreateSprintClick()

        val dialogState = sut.sprintDialogState.value
        assertNotNull(dialogState.startDate)
        assertNotNull(dialogState.endDate)
    }

    // --- create sprint ---

    @Test
    fun `onCreateSprintConfirm success - emits reloadOpenSprints and clears loading`() = runTest {
        createViewModel()
        sut.state.value.onCreateSprintClick()
        sut.sprintDialogState.value.onSetSprintNameValue("Sprint 1")

        sut.reloadOpenSprints.test {
            sut.state.value.onCreateSprintConfirm()
            awaitItem()
        }

        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `onCreateSprintConfirm with empty sprint name - does not emit reloadOpenSprints`() = runTest {
        createViewModel()
        sut.state.value.onCreateSprintClick()
        // sprint name stays empty (default "")

        sut.reloadOpenSprints.test {
            sut.state.value.onCreateSprintConfirm()
            expectNoEvents()
        }
    }
}
