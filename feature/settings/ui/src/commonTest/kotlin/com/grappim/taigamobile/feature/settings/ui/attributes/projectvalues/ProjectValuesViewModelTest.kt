package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.feature.projects.domain.ProjectValueItem
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeProjectValuesRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ProjectValuesViewModelTest {

    private val type = ProjectValueType.US_STATUSES

    private val savedStateHandle = SavedStateHandle(mapOf("typeName" to type.name))

    private val repository = FakeProjectValuesRepository()
    private val sessionStorage = FakeTaigaSessionStorage()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: ProjectValuesViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = ProjectValuesViewModel(
            savedStateHandle = savedStateHandle,
            repository = repository,
            sessionStorage = sessionStorage
        )
    }

    private fun item(id: Long = getRandomLong(), name: String = getRandomString()): ProjectValueItem =
        ProjectValueItem(id = id, name = name, order = 0, project = getRandomLong())

    private fun save(
        name: String = getRandomString(),
        color: String = "#ff0000",
        isClosed: Boolean = false,
        isArchived: Boolean = false,
        value: String = "",
        daysToDue: String = ""
    ) = sut.state.value.onSaveItem(name, color, isClosed, isArchived, value, daysToDue)

    // --- init / loadItems ---

    /**
     * [MainDispatcherRule] uses an unconfined dispatcher, so the `init` block's coroutines have
     * already run by the time [createViewModel] returns — `isLoading = true` is never observable
     * from the outside and this asserts the post-load state.
     */
    @Test
    fun `on init - success - items loaded and type taken from the route`() {
        val loaded = item()
        repository.getProjectValuesResult = persistentListOf(loaded)

        createViewModel()

        with(sut.state.value) {
            assertEquals(type, this.type)
            assertEquals(listOf(loaded), items)
            assertFalse(isLoading)
            assertEquals(NativeText.Empty, error)
        }
        assertEquals(listOf(type), repository.getProjectValuesCalls)
    }

    @Test
    fun `on init - failure - error set and isLoading false`() {
        repository.getProjectValuesThrows = testException

        createViewModel()

        with(sut.state.value) {
            assertTrue(error !is NativeText.Empty)
            assertFalse(isLoading)
            assertTrue(items.isEmpty())
        }
    }

    /**
     * `resultOf` rethrows [CancellationException] instead of turning it into a failure, so neither
     * `onSuccess` nor `onFailure` runs and `isLoading` is left as the load set it.
     */
    @Test
    fun `on init - cancellation - rethrown so loading state is left untouched`() {
        repository.getProjectValuesThrows = CancellationException("cancelled")

        createViewModel()

        with(sut.state.value) {
            assertTrue(isLoading)
            assertEquals(NativeText.Empty, error)
        }
    }

    @Test
    fun `on init - preset colors read from session storage`() {
        sessionStorage.presetColorsResult = persistentListOf("#ff0000", "#00ff00")

        createViewModel()

        assertEquals(listOf("#ff0000", "#00ff00"), sut.state.value.presetColors)
    }

    @Test
    fun `refresh - loads items again`() {
        repository.getProjectValuesResult = persistentListOf(item(name = "old"))
        createViewModel()

        val fresh = item(name = "new")
        repository.getProjectValuesResult = persistentListOf(fresh)
        sut.state.value.refresh()

        assertEquals(listOf("new"), sut.state.value.items.map { it.name })
        assertEquals(listOf(type, type), repository.getProjectValuesCalls)
    }

    // --- edit dialog ---

    @Test
    fun `onAddClick - shows edit dialog with no editing item`() {
        createViewModel()

        sut.state.value.onAddClick()

        with(sut.state.value) {
            assertTrue(isEditDialogVisible)
            assertNull(editingItem)
        }
    }

    @Test
    fun `onEditClick - shows edit dialog with the given item`() {
        createViewModel()
        val existing = item()

        sut.state.value.onEditClick(existing)

        with(sut.state.value) {
            assertTrue(isEditDialogVisible)
            assertEquals(existing, editingItem)
        }
    }

    @Test
    fun `onDismissEditDialog - hides dialog and clears editing item`() {
        createViewModel()
        sut.state.value.onEditClick(item())

        sut.state.value.onDismissEditDialog()

        with(sut.state.value) {
            assertFalse(isEditDialogVisible)
            assertNull(editingItem)
        }
    }

    // --- onSaveItem: create ---

    @Test
    fun `onSaveItem - no editing item - creates the value and appends it to items`() {
        val existing = item(id = 1L)
        repository.getProjectValuesResult = persistentListOf(existing)
        val created = item(id = 2L, name = "created")
        repository.createProjectValueResult = created
        createViewModel()

        sut.state.value.onAddClick()
        save(name = "created", color = "#123456", isClosed = true, isArchived = true, value = "3.5", daysToDue = "7")

        val call = repository.createProjectValueCalls.single()
        assertEquals(
            FakeProjectValuesRepository.SaveCall(
                type = type,
                id = null,
                name = "created",
                color = "#123456",
                isClosed = true,
                isArchived = true,
                value = 3.5,
                daysToDue = 7
            ),
            call
        )
        with(sut.state.value) {
            assertEquals(listOf(existing, created), items)
            assertFalse(isOperationLoading)
            assertFalse(isEditDialogVisible)
            assertNull(editingItem)
        }
        assertTrue(repository.updateProjectValueCalls.isEmpty())
    }

    @Test
    fun `onSaveItem - blank color and unparseable numbers - passes nulls to the repository`() {
        repository.createProjectValueResult = item()
        createViewModel()

        save(color = "   ", value = "not-a-number", daysToDue = "not-a-number")

        with(repository.createProjectValueCalls.single()) {
            assertNull(color)
            assertNull(value)
            assertNull(daysToDue)
        }
    }

    // --- onSaveItem: update ---

    @Test
    fun `onSaveItem - editing item - updates the value and replaces it in items`() {
        val existing = item(id = 42L, name = "before")
        val other = item(id = 43L)
        repository.getProjectValuesResult = persistentListOf(existing, other)
        val updated = existing.copy(name = "after")
        repository.updateProjectValueResult = updated
        createViewModel()

        sut.state.value.onEditClick(existing)
        save(name = "after")

        assertEquals(42L, repository.updateProjectValueCalls.single().id)
        assertEquals(listOf(updated, other), sut.state.value.items)
        assertTrue(repository.createProjectValueCalls.isEmpty())
    }

    /**
     * The saved item is matched into `items` by id, so an update whose result is not already in the
     * list appends instead of replacing. Unreachable from the UI — the edited item always comes
     * from a rendered row — but it is the `index >= 0` else arm. Also covers the update arm's own
     * `color.ifBlank { null }`, which is a separate branch from the create arm's.
     */
    @Test
    fun `onSaveItem - updated item missing from items - appends it`() {
        val existing = item(id = 1L)
        repository.getProjectValuesResult = persistentListOf(existing)
        val updated = item(id = 99L)
        repository.updateProjectValueResult = updated
        createViewModel()

        sut.state.value.onEditClick(item(id = 99L))
        save(color = "  ")

        assertNull(repository.updateProjectValueCalls.single().color)
        assertEquals(listOf(existing, updated), sut.state.value.items)
    }

    @Test
    fun `onSaveItem - failure - snackbar shown and isOperationLoading false`() = runTest {
        repository.createProjectValueThrows = testException
        createViewModel()

        save()

        sut.snackBarMessage.test {
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(sut.state.value.isOperationLoading)
    }

    @Test
    fun `onSaveItem - cancellation - rethrown so isOperationLoading is left set`() {
        repository.createProjectValueThrows = CancellationException("cancelled")
        createViewModel()

        save()

        assertTrue(sut.state.value.isOperationLoading)
    }

    // --- delete dialog ---

    @Test
    fun `onDeleteClick - shows delete dialog with the given item`() {
        createViewModel()
        val target = item()

        sut.state.value.onDeleteClick(target)

        with(sut.state.value) {
            assertTrue(isDeleteDialogVisible)
            assertEquals(target, deletingItem)
        }
    }

    @Test
    fun `onDismissDeleteDialog - hides dialog and clears deleting item`() {
        createViewModel()
        sut.state.value.onDeleteClick(item())

        sut.state.value.onDismissDeleteDialog()

        with(sut.state.value) {
            assertFalse(isDeleteDialogVisible)
            assertNull(deletingItem)
        }
    }

    // --- onConfirmDelete ---

    @Test
    fun `onConfirmDelete - no deleting item - does nothing`() {
        createViewModel()

        sut.state.value.onConfirmDelete(null)

        assertTrue(repository.deleteProjectValueCalls.isEmpty())
        assertFalse(sut.state.value.isOperationLoading)
    }

    @Test
    fun `onConfirmDelete - success - removes the item and passes moveTo through`() {
        val target = item(id = 7L)
        val kept = item(id = 8L)
        repository.getProjectValuesResult = persistentListOf(target, kept)
        createViewModel()

        sut.state.value.onDeleteClick(target)
        sut.state.value.onConfirmDelete(8L)

        assertEquals(
            FakeProjectValuesRepository.DeleteCall(type = type, id = 7L, moveTo = 8L),
            repository.deleteProjectValueCalls.single()
        )
        with(sut.state.value) {
            assertEquals(listOf(kept), items)
            assertFalse(isOperationLoading)
            assertFalse(isDeleteDialogVisible)
            assertNull(deletingItem)
        }
    }

    @Test
    fun `onConfirmDelete - failure - snackbar shown and the item stays in items`() = runTest {
        val target = item(id = 7L)
        repository.getProjectValuesResult = persistentListOf(target)
        createViewModel()
        repository.deleteProjectValueThrows = testException

        sut.state.value.onDeleteClick(target)
        sut.state.value.onConfirmDelete(null)

        sut.snackBarMessage.test {
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        with(sut.state.value) {
            assertEquals(listOf(target), items)
            assertFalse(isOperationLoading)
            assertNull(deletingItem)
        }
    }

    @Test
    fun `onConfirmDelete - cancellation - rethrown so isOperationLoading is left set`() {
        val target = item()
        repository.getProjectValuesResult = persistentListOf(target)
        createViewModel()
        repository.deleteProjectValueThrows = CancellationException("cancelled")

        sut.state.value.onDeleteClick(target)
        sut.state.value.onConfirmDelete(null)

        assertTrue(sut.state.value.isOperationLoading)
    }
}
