package com.grappim.taigamobile.feature.settings.ui.customheaders

import app.cash.turbine.test
import com.grappim.kit.testing.MainDispatcherRule
import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.core.storage.customheaders.CustomHeader
import com.grappim.taigamobile.core.storage.customheaders.CustomHeaderError
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.custom_headers_save_failed
import com.grappim.taigamobile.testing.storage.FakeCustomHeadersStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CustomHeadersViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel(storage: FakeCustomHeadersStorage) =
        CustomHeadersViewModel(customHeadersStorage = storage)

    @Test
    fun `stored headers are offered back as rows`() {
        val sut = createViewModel(FakeCustomHeadersStorage(listOf(TOKEN)))

        assertEquals(listOf(CustomHeaderRow(TOKEN.name, TOKEN.value)), sut.state.value.rows)
    }

    @Test
    fun `nothing changed means nothing to save`() {
        val sut = createViewModel(FakeCustomHeadersStorage(listOf(TOKEN)))

        assertFalse(sut.state.value.isSaveEnabled)
    }

    @Test
    fun `a new header is saved trimmed and blank rows are dropped`() {
        val storage = FakeCustomHeadersStorage()
        val sut = createViewModel(storage)
        val state = { sut.state.value }

        state().onAdd()
        state().onNameChange(0, " X-Proxy-Token ")
        state().onValueChange(0, " secret ")
        state().onAdd()
        assertTrue(state().isSaveEnabled)
        state().onSaveClick()

        assertEquals(listOf(TOKEN), storage.currentHeaders)
        assertEquals(listOf(CustomHeaderRow(TOKEN.name, TOKEN.value)), state().rows)
        assertFalse(state().isSaveEnabled)
        assertFalse(state().isSaving)
    }

    @Test
    fun `removing every header saves an empty list`() {
        val storage = FakeCustomHeadersStorage(listOf(TOKEN))
        val sut = createViewModel(storage)

        sut.state.value.onRemove(0)
        sut.state.value.onSaveClick()

        assertEquals(1, storage.setHeadersCallCount)
        assertEquals(emptyList(), storage.currentHeaders)
        assertTrue(sut.state.value.rows.isEmpty())
    }

    @Test
    fun `an invalid row blocks the save`() {
        val storage = FakeCustomHeadersStorage()
        val sut = createViewModel(storage)
        val state = { sut.state.value }

        state().onAdd()
        state().onNameChange(0, "Host")
        state().onValueChange(0, "taiga.lan")

        assertEquals(CustomHeaderError.ReservedName, state().rows.single().error)
        assertFalse(state().isSaveEnabled)
        state().onSaveClick()
        assertEquals(0, storage.setHeadersCallCount)
    }

    @Test
    fun `Authorization is reserved`() {
        val sut = createViewModel(FakeCustomHeadersStorage())
        val state = { sut.state.value }

        state().onAdd()
        state().onNameChange(0, "authorization")
        state().onValueChange(0, "Basic dXNlcjpwYXNz")

        assertEquals(CustomHeaderError.ReservedName, state().rows.single().error)
    }

    @Test
    fun `a duplicate name marks the second row`() {
        val sut = createViewModel(FakeCustomHeadersStorage(listOf(TOKEN)))
        val state = { sut.state.value }

        state().onAdd()
        state().onNameChange(1, "x-proxy-token")
        state().onValueChange(1, "other")

        assertEquals(listOf(null, CustomHeaderError.DuplicateName), state().rows.map { it.error })
        assertFalse(state().isSaveEnabled)
    }

    @Test
    fun `fixing an error re-enables the save`() {
        val sut = createViewModel(FakeCustomHeadersStorage())
        val state = { sut.state.value }

        state().onAdd()
        state().onNameChange(0, "Bad Name")
        state().onValueChange(0, "v")
        assertEquals(CustomHeaderError.InvalidName, state().rows.single().error)

        state().onNameChange(0, "X-Good")

        assertEquals(null, state().rows.single().error)
        assertTrue(state().isSaveEnabled)
    }

    @Test
    fun `an edit in progress survives a storage emission`() = runTest {
        val storage = FakeCustomHeadersStorage(listOf(TOKEN))
        val sut = createViewModel(storage)

        sut.state.value.onValueChange(0, "edited")
        storage.setHeaders(listOf(OTHER))

        assertEquals("edited", sut.state.value.rows.single().value)
        assertEquals(listOf(OTHER), sut.state.value.saved)
    }

    @Test
    fun `untouched rows follow a storage emission`() = runTest {
        val storage = FakeCustomHeadersStorage(listOf(TOKEN))
        val sut = createViewModel(storage)

        storage.setHeaders(listOf(OTHER))

        assertEquals(listOf(CustomHeaderRow(OTHER.name, OTHER.value)), sut.state.value.rows)
    }

    @Test
    fun `save failure - rows kept, save still offered, snackbar shown`() = runTest {
        val storage = FakeCustomHeadersStorage().apply { setHeadersThrows = true }
        val sut = createViewModel(storage)
        val state = { sut.state.value }

        state().onAdd()
        state().onNameChange(0, TOKEN.name)
        state().onValueChange(0, TOKEN.value)

        sut.snackBarMessage.test {
            state().onSaveClick()

            assertEquals(NativeText.Resource(RString.custom_headers_save_failed), awaitItem())
        }

        assertEquals(1, storage.setHeadersCallCount)
        assertEquals(emptyList(), storage.currentHeaders)
        assertEquals(listOf(CustomHeaderRow(TOKEN.name, TOKEN.value)), state().rows)
        assertFalse(state().isSaving)
        assertTrue(state().isSaveEnabled)
    }

    private companion object {
        val TOKEN = CustomHeader(name = "X-Proxy-Token", value = "secret")
        val OTHER = CustomHeader(name = "CF-Access-Client-Id", value = "abc")
    }
}
