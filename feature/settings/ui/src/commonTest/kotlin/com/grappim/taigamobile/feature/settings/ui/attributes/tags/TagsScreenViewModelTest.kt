package com.grappim.taigamobile.feature.settings.ui.attributes.tags

import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class TagsScreenViewModelTest {

    private val projectsRepository = FakeProjectsRepository()
    private val sessionStorage = FakeTaigaSessionStorage()
    private val tagUIMapper = TagUIMapper()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: TagsScreenViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = TagsScreenViewModel(
            projectsRepository = projectsRepository,
            taigaSessionStorage = sessionStorage,
            tagUIMapper = tagUIMapper
        )
    }

    private fun makeTag(name: String = getRandomString()): Tag = Tag(color = "#ff0000", name = name)

    // --- fetchTagsColors (init) ---

    @Test
    fun `on init - success - tags loaded and isLoading false`() {
        val tag = makeTag()
        projectsRepository.getTagsColorsResult = persistentListOf(tag)

        createViewModel()

        with(sut.state.value) {
            assertFalse(isLoading)
            assertEquals(NativeText.Empty, error)
            assertEquals(listOf(tag.name), tags.map { it.name })
        }
    }

    @Test
    fun `on init - failure - isLoading false and snackbar shown and error set`() = runTest {
        projectsRepository.getTagsColorsThrows = RuntimeException("fetch failed")

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

    // --- refresh ---

    @Test
    fun `refresh - calls fetchTagsColors again with new result`() {
        projectsRepository.getTagsColorsResult = persistentListOf(makeTag("old"))
        createViewModel()

        val newTag = makeTag("new")
        projectsRepository.getTagsColorsResult = persistentListOf(newTag)
        sut.state.value.refresh()

        assertEquals(listOf("new"), sut.state.value.tags.map { it.name })
    }

    // --- onMergeTagsClick ---

    @Test
    fun `onMergeTagsClick - sets isMergeMode true and clears merge state`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        sut.state.value.setDropdownMenuExpanded(true)
        sut.state.value.onMergeTagsClick()

        with(sut.state.value) {
            assertTrue(isMergeMode)
            assertFalse(isDropdownMenuExpanded)
            assertEquals(null, mainTagName)
            assertTrue(tagsToMerge.isEmpty())
        }
    }

    // --- onMainTagSelect ---

    @Test
    fun `onMainTagSelect - sets mainTagName to selected tag`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        val tag = TagUI(name = "main", color = StaticColor(Color.Red))
        sut.state.value.onMainTagSelect(tag)

        assertEquals("main", sut.state.value.mainTagName)
    }

    @Test
    fun `onMainTagSelect - removes selected tag from tagsToMerge if present`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        val tag = TagUI(name = "shared", color = StaticColor(Color.Red))
        sut.state.value.onTagToMergeToggle(tag)
        assertTrue(sut.state.value.tagsToMerge.contains("shared"))

        sut.state.value.onMainTagSelect(tag)

        assertFalse(sut.state.value.tagsToMerge.contains("shared"))
        assertEquals("shared", sut.state.value.mainTagName)
    }

    // --- onTagToMergeToggle ---

    @Test
    fun `onTagToMergeToggle - adds tag to tagsToMerge when not present`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        val tag = TagUI(name = "feature", color = StaticColor(Color.Blue))
        sut.state.value.onTagToMergeToggle(tag)

        assertTrue(sut.state.value.tagsToMerge.contains("feature"))
    }

    @Test
    fun `onTagToMergeToggle - removes tag from tagsToMerge when already present`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        val tag = TagUI(name = "feature", color = StaticColor(Color.Blue))
        sut.state.value.onTagToMergeToggle(tag)
        sut.state.value.onTagToMergeToggle(tag)

        assertFalse(sut.state.value.tagsToMerge.contains("feature"))
    }

    // --- onCancelMerge ---

    @Test
    fun `onCancelMerge - resets isMergeMode and clears merge state`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        sut.state.value.onMergeTagsClick()
        val tag = TagUI(name = "t", color = StaticColor(Color.Green))
        sut.state.value.onMainTagSelect(tag)
        sut.state.value.onTagToMergeToggle(TagUI(name = "other", color = StaticColor(Color.Green)))

        sut.state.value.onCancelMerge()

        with(sut.state.value) {
            assertFalse(isMergeMode)
            assertEquals(null, mainTagName)
            assertTrue(tagsToMerge.isEmpty())
        }
    }

    // --- onConfirmMerge ---

    @Test
    fun `onConfirmMerge - success - calls mixTags and resets merge state`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        val mainTag = TagUI(name = "main", color = StaticColor(Color.Red))
        val mergeTag = TagUI(name = "other", color = StaticColor(Color.Blue))
        sut.state.value.onMergeTagsClick()
        sut.state.value.onMainTagSelect(mainTag)
        sut.state.value.onTagToMergeToggle(mergeTag)

        sut.state.value.onConfirmMerge()

        assertTrue(projectsRepository.mixTagsCalled)
        assertEquals("main", projectsRepository.mixTagsToTag)
        assertEquals(listOf("other"), projectsRepository.mixTagsFromTags)
        with(sut.state.value) {
            assertFalse(isOperationLoading)
            assertFalse(isMergeMode)
            assertEquals(null, mainTagName)
            assertTrue(tagsToMerge.isEmpty())
        }
    }

    @Test
    fun `onConfirmMerge - failure - snackbar shown and isOperationLoading false`() = runTest {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        projectsRepository.mixTagsThrows = RuntimeException("merge failed")

        val mainTag = TagUI(name = "main", color = StaticColor(Color.Red))
        val mergeTag = TagUI(name = "other", color = StaticColor(Color.Blue))
        sut.state.value.onMergeTagsClick()
        sut.state.value.onMainTagSelect(mainTag)
        sut.state.value.onTagToMergeToggle(mergeTag)

        sut.state.value.onConfirmMerge()

        sut.snackBarMessage.test {
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(sut.state.value.isOperationLoading)
    }

    @Test
    fun `onConfirmMerge - no mainTagName - does nothing`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        // do not set mainTagName
        sut.state.value.onTagToMergeToggle(TagUI(name = "other", color = StaticColor(Color.Blue)))
        sut.state.value.onConfirmMerge()

        assertFalse(projectsRepository.mixTagsCalled)
    }

    @Test
    fun `onConfirmMerge - empty tagsToMerge - does nothing`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        sut.state.value.onMainTagSelect(TagUI(name = "main", color = StaticColor(Color.Red)))
        // do not add any tags to merge
        sut.state.value.onConfirmMerge()

        assertFalse(projectsRepository.mixTagsCalled)
    }

    // --- setDropdownMenuExpanded ---

    @Test
    fun `setDropdownMenuExpanded true - updates isDropdownMenuExpanded to true`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        sut.state.value.setDropdownMenuExpanded(true)

        assertTrue(sut.state.value.isDropdownMenuExpanded)
    }

    @Test
    fun `setDropdownMenuExpanded false - updates isDropdownMenuExpanded to false`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        sut.state.value.setDropdownMenuExpanded(true)
        sut.state.value.setDropdownMenuExpanded(false)

        assertFalse(sut.state.value.isDropdownMenuExpanded)
    }

    // --- onAddTagClick ---

    @Test
    fun `onAddTagClick - closes dropdown and shows add dialog`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        sut.state.value.setDropdownMenuExpanded(true)
        sut.state.value.onAddTagClick()

        assertFalse(sut.state.value.isDropdownMenuExpanded)
        assertTrue(sut.tagEditDialogState.value.isVisible)
    }

    // --- onTagDeleteClick + closeDeleteDialog ---

    @Test
    fun `onTagDeleteClick - sets isDeleteTagDialogVisible to true`() {
        projectsRepository.getTagsColorsResult = persistentListOf(makeTag("bug"))
        createViewModel()

        val tag = sut.state.value.tags.first()
        sut.state.value.onTagDeleteClick(tag)

        assertTrue(sut.state.value.isDeleteTagDialogVisible)
    }

    @Test
    fun `closeDeleteDialog - sets isDeleteTagDialogVisible to false`() {
        projectsRepository.getTagsColorsResult = persistentListOf(makeTag("bug"))
        createViewModel()

        val tag = sut.state.value.tags.first()
        sut.state.value.onTagDeleteClick(tag)
        sut.state.value.closeDeleteDialog()

        assertFalse(sut.state.value.isDeleteTagDialogVisible)
    }

    // --- deleteTag ---

    @Test
    fun `deleteTag - success - tag removed from state and isOperationLoading false`() {
        val tagName = "to-delete"
        projectsRepository.getTagsColorsResult = persistentListOf(makeTag(tagName), makeTag("keep"))
        createViewModel()

        val tagUI = sut.state.value.tags.first { it.name == tagName }
        sut.state.value.onTagDeleteClick(tagUI)
        sut.state.value.deleteTag()

        assertFalse(sut.state.value.isOperationLoading)
        assertFalse(sut.state.value.tags.any { it.name == tagName })
        assertTrue(sut.state.value.tags.any { it.name == "keep" })
    }

    @Test
    fun `deleteTag - failure - snackbar shown and tag still in state`() = runTest {
        val tagName = "kept"
        projectsRepository.getTagsColorsResult = persistentListOf(makeTag(tagName))
        createViewModel()

        projectsRepository.deleteTagThrows = RuntimeException("delete failed")

        val tagUI = sut.state.value.tags.first()
        sut.state.value.onTagDeleteClick(tagUI)
        sut.state.value.deleteTag()

        sut.snackBarMessage.test {
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(sut.state.value.isOperationLoading)
        assertTrue(sut.state.value.tags.any { it.name == tagName })
    }

    // --- onSaveTag ---

    @Test
    fun `onSaveTag - success - creates tag, dismisses dialog and reloads tags`() {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        sut.state.value.onAddTagClick()

        val name = getRandomString()
        val newTag = makeTag(name)
        projectsRepository.getTagsColorsResult = persistentListOf(newTag)

        sut.state.value.onSaveClick(name, Color.Red)

        assertTrue(projectsRepository.createTagCalled)
        assertFalse(sut.state.value.isOperationLoading)
        assertFalse(sut.tagEditDialogState.value.isVisible)
        assertEquals(listOf(name), sut.state.value.tags.map { it.name })
    }

    @Test
    fun `onSaveTag - failure - snackbar shown and isOperationLoading false`() = runTest {
        projectsRepository.getTagsColorsResult = persistentListOf()
        createViewModel()

        sut.state.value.onAddTagClick()
        projectsRepository.createTagThrows = RuntimeException("create failed")

        sut.state.value.onSaveClick(getRandomString(), Color.Red)

        sut.snackBarMessage.test {
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(sut.state.value.isOperationLoading)
    }

    // --- initDialogTags ---

    @Test
    fun `initDialogTags - preset colors from storage appear in tagEditDialogState`() = runTest {
        val colors = persistentListOf(Color.Red, Color.Blue)
        sessionStorage.tagPresetColorsResult = colors
        projectsRepository.getTagsColorsResult = persistentListOf()

        createViewModel()

        val dialogState = sut.tagEditDialogState.value
        assertEquals(colors, dialogState.presetColors)
        assertNotNull(dialogState.defaultColor)
    }
}
