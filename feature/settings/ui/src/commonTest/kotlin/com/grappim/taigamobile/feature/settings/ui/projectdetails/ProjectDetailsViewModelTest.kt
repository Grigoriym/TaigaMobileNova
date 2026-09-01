package com.grappim.taigamobile.feature.settings.ui.projectdetails

import app.cash.turbine.test
import com.grappim.taigamobile.feature.projects.domain.ProjectDetails
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

internal class ProjectDetailsViewModelTest {

    private val projectsRepository = FakeProjectsRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: ProjectDetailsViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = ProjectDetailsViewModel(projectsRepository = projectsRepository)
    }

    private fun details(
        name: String = "name",
        description: String = "description",
        isPrivate: Boolean = false,
        isLookingForPeople: Boolean = false,
        lookingForPeopleNote: String = "",
        isContactActivated: Boolean = false
    ): ProjectDetails = ProjectDetails(
        id = 1L,
        name = name,
        description = description,
        isPrivate = isPrivate,
        isLookingForPeople = isLookingForPeople,
        lookingForPeopleNote = lookingForPeopleNote,
        isContactActivated = isContactActivated
    )

    // --- init / loadProjectDetails ---

    /**
     * [MainDispatcherRule] uses an unconfined dispatcher, so the `init` block's coroutine has
     * already run by the time [createViewModel] returns — `isLoading = true` is never observable
     * from the outside and this asserts the post-load state.
     */
    @Test
    fun `on init - success - every field is copied into state`() {
        projectsRepository.getProjectDetailsResult = details(
            name = "Taiga",
            description = "a project",
            isPrivate = true,
            isLookingForPeople = true,
            lookingForPeopleNote = "join us",
            isContactActivated = true
        )

        createViewModel()

        with(sut.state.value) {
            assertEquals("Taiga", name)
            assertEquals("a project", description)
            assertTrue(isPrivate)
            assertTrue(isLookingForPeople)
            assertEquals("join us", lookingForPeopleNote)
            assertTrue(isContactActivated)
            assertFalse(isLoading)
            assertEquals(NativeText.Empty, error)
        }
    }

    @Test
    fun `on init - failure - error set and isLoading false`() {
        projectsRepository.getProjectDetailsThrows = testException

        createViewModel()

        with(sut.state.value) {
            assertTrue(error !is NativeText.Empty)
            assertFalse(isLoading)
            assertEquals("", name)
        }
    }

    /**
     * `resultOf` rethrows [CancellationException] instead of turning it into a failure, so neither
     * `onSuccess` nor `onFailure` runs and `isLoading` is left as the load set it.
     */
    @Test
    fun `on init - cancellation - rethrown so loading state is left untouched`() {
        projectsRepository.getProjectDetailsThrows = CancellationException("cancelled")

        createViewModel()

        with(sut.state.value) {
            assertTrue(isLoading)
            assertEquals(NativeText.Empty, error)
        }
    }

    // --- text fields and toggles ---

    @Test
    fun `onNameChange updates the field`() {
        projectsRepository.getProjectDetailsResult = details()
        createViewModel()

        sut.state.value.onNameChange("renamed")

        assertEquals("renamed", sut.state.value.name)
    }

    @Test
    fun `onDescriptionChange updates the field`() {
        projectsRepository.getProjectDetailsResult = details()
        createViewModel()

        sut.state.value.onDescriptionChange("new description")

        assertEquals("new description", sut.state.value.description)
    }

    @Test
    fun `onIsPrivateChange updates the flag`() {
        projectsRepository.getProjectDetailsResult = details()
        createViewModel()

        sut.state.value.onIsPrivateChange(true)

        assertTrue(sut.state.value.isPrivate)
    }

    @Test
    fun `onIsLookingForPeopleChange updates the flag`() {
        projectsRepository.getProjectDetailsResult = details()
        createViewModel()

        sut.state.value.onIsLookingForPeopleChange(true)

        assertTrue(sut.state.value.isLookingForPeople)
    }

    @Test
    fun `onLookingForPeopleNoteChange updates the field`() {
        projectsRepository.getProjectDetailsResult = details()
        createViewModel()

        sut.state.value.onLookingForPeopleNoteChange("we are hiring")

        assertEquals("we are hiring", sut.state.value.lookingForPeopleNote)
    }

    @Test
    fun `onIsContactActivatedChange updates the flag`() {
        projectsRepository.getProjectDetailsResult = details()
        createViewModel()

        sut.state.value.onIsContactActivatedChange(true)

        assertTrue(sut.state.value.isContactActivated)
    }

    // --- save ---

    /**
     * `_navigateBack` is a rendezvous channel, so the collector has to be waiting before `save`
     * sends — hence triggering the save inside the turbine block. Everything the coroutine does
     * before the send (the `isSaving = false` update) has already happened once `awaitItem`
     * returns.
     *
     * `save` reads `_state.value` rather than a separately tracked "edited" value, and `init`
     * copies the fake's result straight into that state — so configuring the fake's result to
     * already be the target state reaches it in one step, with no `onXChange` chain needed.
     */
    @Test
    fun `onSaveClick - success - sends the edited state to the repository and navigates back`() = runTest {
        projectsRepository.getProjectDetailsResult = details(
            name = "edited",
            description = "edited description",
            isPrivate = true,
            isLookingForPeople = true,
            lookingForPeopleNote = "join us",
            isContactActivated = true
        )
        createViewModel()

        sut.navigateBack.test {
            sut.state.value.onSaveClick()
            awaitItem()
        }

        assertEquals(
            FakeProjectsRepository.UpdateProjectCall(
                name = "edited",
                description = "edited description",
                isPrivate = true,
                isLookingForPeople = true,
                lookingForPeopleNote = "join us",
                isContactActivated = true
            ),
            projectsRepository.updateProjectCalls.single()
        )
        assertFalse(sut.state.value.isSaving)
    }

    /**
     * `showSnackbarSuspend` is itself a rendezvous send, so the `isSaving = false` update after it
     * only runs once the message has been received — assert it after the turbine block, not
     * before.
     */
    @Test
    fun `onSaveClick - failure - snackbar shown and isSaving false`() = runTest {
        projectsRepository.getProjectDetailsResult = details()
        createViewModel()
        projectsRepository.updateProjectThrows = testException

        sut.snackBarMessage.test {
            sut.state.value.onSaveClick()
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(sut.state.value.isSaving)
    }

    @Test
    fun `onSaveClick - cancellation - rethrown so isSaving is left set`() {
        projectsRepository.getProjectDetailsResult = details()
        createViewModel()
        projectsRepository.updateProjectThrows = CancellationException("cancelled")

        sut.state.value.onSaveClick()

        assertTrue(sut.state.value.isSaving)
    }
}
