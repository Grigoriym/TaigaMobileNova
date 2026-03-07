package com.grappim.taigamobile.feature.wiki.ui.bookmark.create

import app.cash.turbine.test
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeWikiRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WikiCreateBookmarkViewModelTest {

    private val wikiRepository = FakeWikiRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: WikiCreateBookmarkViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = WikiCreateBookmarkViewModel(wikiRepository = wikiRepository)
    }

    private fun makeWikiLink(id: Long = getRandomLong(), title: String = getRandomString()): WikiLink =
        WikiLink(ref = getRandomString(), id = id, order = 0L, title = title)

    // --- initial state ---

    @Test
    fun `initial state has empty title and no error`() {
        createViewModel()

        val state = sut.state.value
        assertEquals("", state.title)
        assertTrue(state.error is NativeText.Empty)
        assertFalse(state.isLoading)
    }

    // --- setTitle ---

    @Test
    fun `setTitle updates title in state`() {
        createViewModel()

        val title = getRandomString()
        sut.state.value.setTitle(title)

        assertEquals(title, sut.state.value.title)
    }

    @Test
    fun `setTitle clears existing error`() = runTest {
        wikiRepository.createWikiLinkThrows = testException
        createViewModel()

        sut.state.value.setTitle(getRandomString())
        sut.state.value.onCreateWikiBookmark()

        // consume the error state
        assertTrue(sut.state.value.error !is NativeText.Empty)

        sut.state.value.setTitle(getRandomString())

        assertTrue(sut.state.value.error is NativeText.Empty)
    }

    // --- createWikiBookmark success ---

    @Test
    fun `onCreateWikiBookmark success emits WikiLink via creationResult`() = runTest {
        val link = makeWikiLink()
        wikiRepository.createWikiLinkResult = link
        createViewModel()

        val title = getRandomString()
        sut.state.value.setTitle(title)

        sut.creationResult.test {
            sut.state.value.onCreateWikiBookmark()

            val emitted = awaitItem()
            assertEquals(link.id, emitted.id)
            assertEquals(link.title, emitted.title)
        }
    }

    @Test
    fun `onCreateWikiBookmark success passes title to repository`() = runTest {
        val link = makeWikiLink()
        wikiRepository.createWikiLinkResult = link
        createViewModel()

        val title = getRandomString()
        sut.state.value.setTitle(title)

        sut.creationResult.test {
            sut.state.value.onCreateWikiBookmark()
            awaitItem()
        }

        assertTrue(wikiRepository.createWikiLinkCalled)
        assertEquals(title, wikiRepository.createWikiLinkTitle)
    }

    @Test
    fun `onCreateWikiBookmark success sets isLoading to false`() = runTest {
        val link = makeWikiLink()
        wikiRepository.createWikiLinkResult = link
        createViewModel()

        sut.creationResult.test {
            sut.state.value.onCreateWikiBookmark()
            awaitItem()
        }

        assertFalse(sut.state.value.isLoading)
    }

    // --- createWikiBookmark failure ---

    @Test
    fun `onCreateWikiBookmark failure sets error in state`() = runTest {
        wikiRepository.createWikiLinkThrows = testException
        createViewModel()

        sut.state.value.onCreateWikiBookmark()

        assertTrue(sut.state.value.error !is NativeText.Empty)
    }

    @Test
    fun `onCreateWikiBookmark failure sets isLoading to false`() = runTest {
        wikiRepository.createWikiLinkThrows = testException
        createViewModel()

        sut.state.value.onCreateWikiBookmark()

        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `onCreateWikiBookmark failure does not emit to creationResult`() = runTest {
        wikiRepository.createWikiLinkThrows = testException
        createViewModel()

        sut.creationResult.test {
            sut.state.value.onCreateWikiBookmark()
            expectNoEvents()
        }
    }
}
