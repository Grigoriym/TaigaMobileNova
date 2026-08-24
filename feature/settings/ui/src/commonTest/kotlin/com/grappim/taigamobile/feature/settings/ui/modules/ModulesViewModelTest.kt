package com.grappim.taigamobile.feature.settings.ui.modules

import app.cash.turbine.test
import com.grappim.taigamobile.feature.projects.domain.ProjectModules
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ModulesViewModelTest {

    private val projectsRepository = FakeProjectsRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: ModulesViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = ModulesViewModel(projectsRepository = projectsRepository)
    }

    private fun modules(
        isEpicsActivated: Boolean = false,
        isBacklogActivated: Boolean = false,
        isKanbanActivated: Boolean = false,
        isIssuesActivated: Boolean = false,
        isWikiActivated: Boolean = false,
        totalMilestones: Int? = null,
        totalStoryPoints: Double? = null
    ): ProjectModules = ProjectModules(
        isEpicsActivated = isEpicsActivated,
        isBacklogActivated = isBacklogActivated,
        isKanbanActivated = isKanbanActivated,
        isIssuesActivated = isIssuesActivated,
        isWikiActivated = isWikiActivated,
        isContactActivated = false,
        totalMilestones = totalMilestones,
        totalStoryPoints = totalStoryPoints,
        videoconferencesExtraData = null
    )

    // --- init / loadModules ---

    /**
     * [MainDispatcherRule] uses an unconfined dispatcher, so the `init` block's coroutine has
     * already run by the time [createViewModel] returns — `isLoading = true` is never observable
     * from the outside and this asserts the post-load state.
     */
    @Test
    fun `on init - success - every module flag and both numbers are copied into state`() {
        projectsRepository.getProjectModulesResult = modules(
            isEpicsActivated = true,
            isBacklogActivated = true,
            isKanbanActivated = true,
            isIssuesActivated = true,
            isWikiActivated = true,
            totalMilestones = 7,
            totalStoryPoints = 3.5
        )

        createViewModel()

        with(sut.state.value) {
            assertTrue(isEpicsActivated)
            assertTrue(isBacklogActivated)
            assertTrue(isKanbanActivated)
            assertTrue(isIssuesActivated)
            assertTrue(isWikiActivated)
            assertEquals("7", totalMilestones)
            assertEquals("3.5", totalStoryPoints)
            assertFalse(isLoading)
            assertEquals(NativeText.Empty, error)
        }
    }

    /**
     * The `?: ""` arms of `totalMilestones` / `totalStoryPoints` — both are nullable on
     * [ProjectModules] and the state holds them as text-field strings.
     */
    @Test
    fun `on init - success with null numbers - both number fields are blank`() {
        projectsRepository.getProjectModulesResult = modules(
            totalMilestones = null,
            totalStoryPoints = null
        )

        createViewModel()

        with(sut.state.value) {
            assertEquals("", totalMilestones)
            assertEquals("", totalStoryPoints)
            assertFalse(isLoading)
        }
    }

    @Test
    fun `on init - failure - error set and isLoading false`() {
        projectsRepository.getProjectModulesThrows = testException

        createViewModel()

        with(sut.state.value) {
            assertTrue(error !is NativeText.Empty)
            assertFalse(isLoading)
            assertFalse(isEpicsActivated)
        }
    }

    /**
     * `resultOf` rethrows [CancellationException] instead of turning it into a failure, so neither
     * `onSuccess` nor `onFailure` runs and `isLoading` is left as the load set it.
     */
    @Test
    fun `on init - cancellation - rethrown so loading state is left untouched`() {
        projectsRepository.getProjectModulesThrows = CancellationException("cancelled")

        createViewModel()

        with(sut.state.value) {
            assertTrue(isLoading)
            assertEquals(NativeText.Empty, error)
        }
    }

    // --- toggles and number fields ---

    @Test
    fun `onEpicsActivatedChange updates the flag`() {
        projectsRepository.getProjectModulesResult = modules()
        createViewModel()

        sut.state.value.onEpicsActivatedChange(true)

        assertTrue(sut.state.value.isEpicsActivated)
    }

    @Test
    fun `onBacklogActivatedChange updates the flag`() {
        projectsRepository.getProjectModulesResult = modules()
        createViewModel()

        sut.state.value.onBacklogActivatedChange(true)

        assertTrue(sut.state.value.isBacklogActivated)
    }

    @Test
    fun `onKanbanActivatedChange updates the flag`() {
        projectsRepository.getProjectModulesResult = modules()
        createViewModel()

        sut.state.value.onKanbanActivatedChange(true)

        assertTrue(sut.state.value.isKanbanActivated)
    }

    @Test
    fun `onIssuesActivatedChange updates the flag`() {
        projectsRepository.getProjectModulesResult = modules()
        createViewModel()

        sut.state.value.onIssuesActivatedChange(true)

        assertTrue(sut.state.value.isIssuesActivated)
    }

    @Test
    fun `onWikiActivatedChange updates the flag`() {
        projectsRepository.getProjectModulesResult = modules()
        createViewModel()

        sut.state.value.onWikiActivatedChange(true)

        assertTrue(sut.state.value.isWikiActivated)
    }

    @Test
    fun `onTotalMilestonesChange updates the field`() {
        projectsRepository.getProjectModulesResult = modules(totalMilestones = 1)
        createViewModel()

        sut.state.value.onTotalMilestonesChange("42")

        assertEquals("42", sut.state.value.totalMilestones)
    }

    @Test
    fun `onTotalStoryPointsChange updates the field`() {
        projectsRepository.getProjectModulesResult = modules(totalStoryPoints = 1.0)
        createViewModel()

        sut.state.value.onTotalStoryPointsChange("12.5")

        assertEquals("12.5", sut.state.value.totalStoryPoints)
    }

    // --- save ---

    /**
     * `_navigateBack` is a rendezvous channel, so the collector has to be waiting before `save`
     * sends — hence triggering the save inside the turbine block. Everything the coroutine does
     * before the send (the `isSaving = false` update) has already happened once `awaitItem`
     * returns.
     */
    @Test
    fun `onSaveClick - success - sends the edited state to the repository and navigates back`() = runTest {
        projectsRepository.getProjectModulesResult = modules()
        createViewModel()
        with(sut.state.value) {
            onEpicsActivatedChange(true)
            onKanbanActivatedChange(true)
            onWikiActivatedChange(true)
            onTotalMilestonesChange("9")
            onTotalStoryPointsChange("4.25")
        }

        sut.navigateBack.test {
            sut.state.value.onSaveClick()
            awaitItem()
        }

        assertEquals(
            FakeProjectsRepository.UpdateModulesCall(
                isEpicsActivated = true,
                isBacklogActivated = false,
                isKanbanActivated = true,
                isIssuesActivated = false,
                isWikiActivated = true,
                totalMilestones = 9,
                totalStoryPoints = 4.25
            ),
            projectsRepository.updateModulesCalls.single()
        )
        assertFalse(sut.state.value.isSaving)
    }

    /**
     * The number fields are free text, so `toIntOrNull` / `toDoubleOrNull` are what decide whether
     * the module limits are sent at all.
     */
    @Test
    fun `onSaveClick - unparseable numbers - passes nulls to the repository`() = runTest {
        projectsRepository.getProjectModulesResult = modules(totalMilestones = 1, totalStoryPoints = 2.0)
        createViewModel()
        sut.state.value.onTotalMilestonesChange("not-a-number")
        sut.state.value.onTotalStoryPointsChange("")

        sut.navigateBack.test {
            sut.state.value.onSaveClick()
            awaitItem()
        }

        with(projectsRepository.updateModulesCalls.single()) {
            assertEquals(null, totalMilestones)
            assertEquals(null, totalStoryPoints)
        }
    }

    /**
     * `showSnackbarSuspend` is itself a rendezvous send, so the `isSaving = false` update after it
     * only runs once the message has been received — assert it after the turbine block, not
     * before.
     */
    @Test
    fun `onSaveClick - failure - snackbar shown and isSaving false`() = runTest {
        projectsRepository.getProjectModulesResult = modules()
        createViewModel()
        projectsRepository.updateModulesThrows = testException

        sut.snackBarMessage.test {
            sut.state.value.onSaveClick()
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(sut.state.value.isSaving)
    }

    @Test
    fun `onSaveClick - cancellation - rethrown so isSaving is left set`() {
        projectsRepository.getProjectModulesResult = modules()
        createViewModel()
        projectsRepository.updateModulesThrows = CancellationException("cancelled")

        sut.state.value.onSaveClick()

        assertTrue(sut.state.value.isSaving)
    }
}
