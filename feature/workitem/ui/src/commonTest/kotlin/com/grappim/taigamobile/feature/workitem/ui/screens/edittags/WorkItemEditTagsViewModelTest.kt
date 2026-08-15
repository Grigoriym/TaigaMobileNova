package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.StaticColor
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WorkItemEditTagsViewModelTest {

    private val workItemId = getRandomLong()
    private val taskIdentifier: TaskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)

    private val bugTag = Tag(color = "#FF0000", name = "bug")
    private val featureTag = Tag(color = "#00FF00", name = "feature")

    private val tagUIMapper = TagUIMapper()
    private val projectsRepository = FakeProjectsRepository()
    private val workItemEditStateRepository = WorkItemEditStateRepository()
    private val sessionStorage = FakeTaigaSessionStorage()
    private val mainDispatcherRule = MainDispatcherRule()

    private val route = WorkItemEditTagsNavDestination(workItemId = workItemId, taskIdentifier = taskIdentifier)

    private lateinit var sut: WorkItemEditTagsViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
        projectsRepository.getTagsColorsResult = persistentListOf(bugTag, featureTag)
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = WorkItemEditTagsViewModel(
            tagUIMapper = tagUIMapper,
            workItemEditStateRepository = workItemEditStateRepository,
            projectsRepository = projectsRepository,
            taigaSessionStorage = sessionStorage,
            route = route
        )
    }

    /**
     * The tags in state are keyed by name, so this is how a test picks the one it wants to click.
     */
    private fun tagNamed(name: String): SelectableTagUI = sut.state.value.tags.first { it.name == name }

    private fun selectedNames(): List<String> = sut.state.value.currentSelectedTags.map { it.name }

    // --- init / fetchTags ---

    /**
     * `MainDispatcherRule` uses `UnconfinedTestDispatcher`, so the `init` block's launches have
     * already run to completion by the time `createViewModel()` returns.
     */
    @Test
    fun `on init tags are loaded with the already selected ones marked and sorted first`() {
        workItemEditStateRepository.setTags(
            workItemId = workItemId,
            type = taskIdentifier,
            tags = persistentListOf(SelectableTagUI(name = "feature", color = Color.Green, isSelected = true))
        )

        createViewModel()

        assertEquals(listOf("feature", "bug"), sut.state.value.tags.map { it.name })
        assertTrue(tagNamed("feature").isSelected)
        assertFalse(tagNamed("bug").isSelected)
    }

    @Test
    fun `on init the selection is read from the edit state repository`() {
        val alreadySelected = SelectableTagUI(name = "feature", color = Color.Green, isSelected = true)
        workItemEditStateRepository.setTags(
            workItemId = workItemId,
            type = taskIdentifier,
            tags = persistentListOf(alreadySelected)
        )

        createViewModel()

        assertEquals(persistentListOf(alreadySelected), sut.state.value.currentSelectedTags)
        assertEquals(persistentListOf(alreadySelected), sut.state.value.originalSelectedTags)
    }

    @Test
    fun `on init with nothing selected the selection is empty`() {
        createViewModel()

        assertEquals(2, sut.state.value.tags.size)
        assertTrue(sut.state.value.currentSelectedTags.isEmpty())
        assertTrue(sut.state.value.originalSelectedTags.isEmpty())
    }

    @Test
    fun `on init failure state stays empty`() {
        projectsRepository.getTagsColorsThrows = testException

        createViewModel()

        assertTrue(sut.state.value.tags.isEmpty())
        assertTrue(sut.state.value.currentSelectedTags.isEmpty())
    }

    /**
     * Covers `resultOf`'s `catch (e: CancellationException) { throw e }` rethrow, which is inlined
     * into `fetchTags`. The observable outcome is the same as the failure path above — neither
     * `onSuccess` nor `onFailure` runs — so only the branch distinguishes the two.
     */
    @Test
    fun `on init a cancellation is rethrown instead of being swallowed`() {
        projectsRepository.getTagsColorsThrows = CancellationException("cancelled")

        createViewModel()

        assertTrue(sut.state.value.tags.isEmpty())
    }

    @Test
    fun `on init the dialog preset colors are loaded from storage`() {
        sessionStorage.tagPresetColorsResult = persistentListOf(Color.Red, Color.Blue)

        createViewModel()

        assertEquals(persistentListOf(Color.Red, Color.Blue), sut.tagEditDialogState.value.presetColors)
        assertEquals(Color.Red, (sut.tagEditDialogState.value.defaultColor as StaticColor).color)
    }

    // --- onTagClick ---

    @Test
    fun `onTagClick selects a tag that was not selected`() {
        createViewModel()

        sut.state.value.onTagClick(tagNamed("bug"))

        assertTrue(tagNamed("bug").isSelected)
        assertFalse(tagNamed("feature").isSelected)
        assertEquals(listOf("bug"), selectedNames())
    }

    @Test
    fun `onTagClick deselects a tag that was already selected`() {
        createViewModel()
        sut.state.value.onTagClick(tagNamed("bug"))

        sut.state.value.onTagClick(tagNamed("bug"))

        assertFalse(tagNamed("bug").isSelected)
        assertTrue(selectedNames().isEmpty())
    }

    @Test
    fun `onTagClick with a tag that is not in the list leaves the selection unchanged`() {
        createViewModel()
        sut.state.value.onTagClick(tagNamed("bug"))

        sut.state.value.onTagClick(SelectableTagUI(name = "unknown", color = Color.Yellow))

        assertEquals(listOf("bug"), selectedNames())
        assertEquals(listOf("bug", "feature"), sut.state.value.tags.map { it.name })
    }

    // --- dialog visibility ---

    @Test
    fun `setIsDialogVisible true shows the dialog`() {
        createViewModel()

        sut.state.value.setIsDialogVisible(true)

        assertTrue(sut.state.value.isDialogVisible)
    }

    @Test
    fun `setIsDialogVisible false hides the dialog`() {
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.state.value.setIsDialogVisible(false)

        assertFalse(sut.state.value.isDialogVisible)
    }

    @Test
    fun `onBackClick toggles the dialog visibility`() {
        createViewModel()

        sut.state.value.onBackClick()
        assertTrue(sut.state.value.isDialogVisible)

        sut.state.value.onBackClick()
        assertFalse(sut.state.value.isDialogVisible)
    }

    @Test
    fun `onAddTagClick opens the add tag dialog`() {
        createViewModel()

        sut.state.value.onAddTagClick()

        assertTrue(sut.tagEditDialogState.value.isVisible)
        assertNull(sut.tagEditDialogState.value.tagUI)
    }

    // --- onGoingBack / notifyChange ---

    @Test
    fun `onSaveTags sends the current tags to the edit state repository`() = runTest {
        createViewModel()
        sut.state.value.onTagClick(tagNamed("bug"))

        var received: PersistentList<SelectableTagUI>? = null
        val collectJob = launch {
            workItemEditStateRepository.getTagsFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.onSaveTags()
            awaitItem()
        }

        collectJob.join()
        val tags = assertNotNull(received)
        assertEquals(listOf("bug"), tags.map { it.name })
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and changed tags sends them to the repository`() = runTest {
        createViewModel()
        sut.state.value.onTagClick(tagNamed("bug"))

        var received: PersistentList<SelectableTagUI>? = null
        val collectJob = launch {
            workItemEditStateRepository.getTagsFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.join()
        val tags = assertNotNull(received)
        assertEquals(listOf("bug"), tags.map { it.name })
    }

    @Test
    fun `shouldGoBackWithCurrentValue false does not send tags even when they changed`() = runTest {
        createViewModel()
        sut.state.value.onTagClick(tagNamed("bug"))

        var received: PersistentList<SelectableTagUI>? = null
        val collectJob = launch {
            workItemEditStateRepository.getTagsFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }

        collectJob.cancel()
        assertNull(received)
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and unchanged tags does not send tags`() = runTest {
        workItemEditStateRepository.setTags(
            workItemId = workItemId,
            type = taskIdentifier,
            tags = persistentListOf(SelectableTagUI(name = "feature", color = Color.Green, isSelected = true))
        )
        createViewModel()

        var received: PersistentList<SelectableTagUI>? = null
        val collectJob = launch {
            workItemEditStateRepository.getTagsFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.cancel()
        assertNull(received)
    }

    @Test
    fun `shouldGoBackWithCurrentValue hides the dialog before navigating back`() = runTest {
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }

        assertFalse(sut.state.value.isDialogVisible)
    }

    // --- onSaveClick ---

    /**
     * The refetch triggered by a successful save is the only path on which `fetchTags` is not an
     * initial load, so this is what covers its `isInitialLoad = false` branches: the selection is
     * carried over from state rather than re-read from the edit state repository.
     */
    @Test
    fun `onSaveClick creates the tag selects it and refetches carrying the current selection over`() {
        createViewModel()
        sut.state.value.onTagClick(tagNamed("bug"))
        projectsRepository.getTagsColorsResult = persistentListOf(
            bugTag,
            featureTag,
            Tag(color = "#0000FF", name = "new-tag")
        )

        sut.state.value.onSaveClick("new-tag", Color.Blue)

        assertTrue(projectsRepository.createTagCalled)
        assertEquals(3, sut.state.value.tags.size)
        assertEquals(setOf("bug", "new-tag"), selectedNames().toSet())
        assertTrue(tagNamed("new-tag").isSelected)
        assertTrue(tagNamed("bug").isSelected)
        assertFalse(tagNamed("feature").isSelected)
    }

    @Test
    fun `onSaveClick dismisses the dialog on success`() {
        createViewModel()
        sut.state.value.onAddTagClick()

        sut.state.value.onSaveClick("new-tag", Color.Blue)

        assertFalse(sut.tagEditDialogState.value.isVisible)
    }

    @Test
    fun `onSaveClick failure surfaces the error in the dialog state and does not change the selection`() {
        createViewModel()
        projectsRepository.createTagThrows = testException

        sut.state.value.onSaveClick("new-tag", Color.Blue)

        assertNotNull(sut.tagEditDialogState.value.errorMessage)
        assertFalse(sut.tagEditDialogState.value.isLoading)
        assertTrue(selectedNames().isEmpty())
    }
}
