package com.grappim.taigamobile.feature.workitem.ui.delegates.tagedit

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TagEditDialogDelegateImplTest {

    private val fakeProjectsRepository = FakeProjectsRepository()
    private val fakeSessionStorage = FakeTaigaSessionStorage()

    private val delegate = TagEditDialogDelegateImpl(
        projectsRepository = fakeProjectsRepository,
        taigaSessionStorage = fakeSessionStorage
    )

    @Test
    fun `initDialogTags sets preset colors from storage`() = runTest {
        val colors = persistentListOf(Color.Red, Color.Blue)
        fakeSessionStorage.tagPresetColorsResult = colors

        delegate.initDialogTags()

        val state = delegate.tagEditDialogState.value
        assertEquals(colors, state.presetColors)
        assertEquals(Color.Red, (state.defaultColor as StaticColor).color)
    }

    @Test
    fun `initDialogTags uses Gray as default color when storage returns empty list`() = runTest {
        fakeSessionStorage.tagPresetColorsResult = persistentListOf()

        delegate.initDialogTags()

        val state = delegate.tagEditDialogState.value
        assertEquals(Color.Gray, (state.defaultColor as StaticColor).color)
    }

    @Test
    fun `showAddDialog makes dialog visible with null tag and add_tag title`() {
        delegate.showAddDialog()

        val state = delegate.tagEditDialogState.value
        assertTrue(state.isVisible)
        assertNull(state.tagUI)
        assertTrue(state.dialogTitle is NativeText.Resource)
    }

    @Test
    fun `showEditDialog makes dialog visible with the tag and edit_tag title`() {
        val tag = TagUI(name = "bug", color = StaticColor(Color.Red))

        delegate.showEditDialog(tag)

        val state = delegate.tagEditDialogState.value
        assertTrue(state.isVisible)
        assertEquals(tag, state.tagUI)
        assertTrue(state.dialogTitle is NativeText.Resource)
    }

    @Test
    fun `dismissEditDialog hides dialog and clears tag and loading`() {
        delegate.showAddDialog()
        delegate.dismissEditDialog()

        val state = delegate.tagEditDialogState.value
        assertFalse(state.isVisible)
        assertNull(state.tagUI)
        assertFalse(state.isLoading)
    }

    @Test
    fun `handleSaveTag creates tag when no tag being edited`() = runTest {
        var successCalled = false

        delegate.handleSaveTag(
            name = "new-tag",
            color = Color.Green,
            doOnSuccess = { successCalled = true }
        )

        assertTrue(fakeProjectsRepository.createTagCalled)
        assertTrue(successCalled)
        assertFalse(delegate.tagEditDialogState.value.isVisible)
    }

    @Test
    fun `handleSaveTag on create failure sets error and calls doOnError`() = runTest {
        val error = RuntimeException("create failed")
        fakeProjectsRepository.createTagThrows = error
        var errorThrowable: Throwable? = null

        delegate.handleSaveTag(
            name = "new-tag",
            color = Color.Green,
            doOnError = { errorThrowable = it }
        )

        assertEquals(error, errorThrowable)
        assertFalse(delegate.tagEditDialogState.value.isLoading)
        assertNotNull(delegate.tagEditDialogState.value.errorMessage)
    }

    @Test
    fun `handleSaveTag edits tag when a tag is being edited`() = runTest {
        val tag = TagUI(name = "old-name", color = StaticColor(Color.Blue))
        delegate.showEditDialog(tag)
        var successCalled = false

        delegate.handleSaveTag(
            name = "new-name",
            color = Color.Red,
            doOnSuccess = { successCalled = true }
        )

        assertEquals("old-name", fakeProjectsRepository.editTagFromTagName)
        assertEquals("new-name", fakeProjectsRepository.editTagToTagName)
        assertTrue(successCalled)
        assertFalse(delegate.tagEditDialogState.value.isVisible)
    }

    @Test
    fun `handleSaveTag passes null toTagName when name is unchanged`() = runTest {
        val tag = TagUI(name = "same-name", color = StaticColor(Color.Blue))
        delegate.showEditDialog(tag)

        delegate.handleSaveTag(
            name = "same-name",
            color = Color.Red
        )

        assertEquals("same-name", fakeProjectsRepository.editTagFromTagName)
        assertNull(fakeProjectsRepository.editTagToTagName)
    }

    @Test
    fun `handleSaveTag on edit failure sets error and calls doOnError`() = runTest {
        val tag = TagUI(name = "old-name", color = StaticColor(Color.Blue))
        delegate.showEditDialog(tag)
        val error = RuntimeException("edit failed")
        fakeProjectsRepository.editTagThrows = error
        var errorThrowable: Throwable? = null

        delegate.handleSaveTag(
            name = "new-name",
            color = Color.Red,
            doOnError = { errorThrowable = it }
        )

        assertEquals(error, errorThrowable)
        assertFalse(delegate.tagEditDialogState.value.isLoading)
        assertNotNull(delegate.tagEditDialogState.value.errorMessage)
    }

    @Test
    fun `dismissEditDialog after showEditDialog clears tagToEdit so next save creates`() = runTest {
        val tag = TagUI(name = "edit-me", color = StaticColor(Color.Blue))
        delegate.showEditDialog(tag)
        delegate.dismissEditDialog()

        delegate.handleSaveTag(name = "brand-new", color = Color.Cyan)

        assertTrue(fakeProjectsRepository.createTagCalled)
    }
}
