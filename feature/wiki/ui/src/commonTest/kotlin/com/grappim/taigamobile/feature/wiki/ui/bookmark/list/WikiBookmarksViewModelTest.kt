package com.grappim.taigamobile.feature.wiki.ui.bookmark.list

import app.cash.turbine.test
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeWikiRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WikiBookmarksViewModelTest {

    private val wikiRepository = FakeWikiRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: WikiBookmarksViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = WikiBookmarksViewModel(
            wikiRepository = wikiRepository,
            projectsRepository = projectsRepository
        )
    }

    private fun makeWikiLink(id: Long = getRandomLong(), title: String = getRandomString()): WikiLink =
        WikiLink(ref = getRandomString(), id = id, order = 0L, title = title)

    // --- initial load ---

    @Test
    fun `on init success bookmarks are loaded into state`() {
        val link = makeWikiLink()
        wikiRepository.getWikiLinksResult = persistentListOf(link)

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.bookmarks.size)
        assertEquals(link.id, state.bookmarks[0].id)
        assertEquals(link.title, state.bookmarks[0].title)
        assertEquals(link.ref, state.bookmarks[0].slug)
    }

    @Test
    fun `on init success state has no error`() {
        createViewModel()

        assertTrue(sut.state.value.error is NativeText.Empty)
    }

    @Test
    fun `on init failure state has error and is not loading`() = runTest {
        wikiRepository.getWikiLinksThrows = testException

        createViewModel()

        sut.snackBarMessage.test {
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error !is NativeText.Empty)
        assertTrue(state.bookmarks.isEmpty())
    }

    // --- permissions ---

    @Test
    fun `canAddWikiLink is true when permission is present`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.ADD_WIKI_LINK)

        createViewModel()

        assertTrue(sut.state.value.canAddWikiLink)
    }

    @Test
    fun `canAddWikiLink is false when permission is absent`() {
        projectsRepository.permissions = persistentListOf()

        createViewModel()

        assertFalse(sut.state.value.canAddWikiLink)
    }

    @Test
    fun `canDeleteWikiLink is true when permission is present`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.DELETE_WIKI_LINK)

        createViewModel()

        assertTrue(sut.state.value.canDeleteWikiLink)
    }

    @Test
    fun `canDeleteWikiLink is false when permission is absent`() {
        projectsRepository.permissions = persistentListOf()

        createViewModel()

        assertFalse(sut.state.value.canDeleteWikiLink)
    }

    // --- delete dialog ---

    @Test
    fun `onDeleteClick shows dialog and sets bookmarkIdToDelete`() {
        createViewModel()

        val id = getRandomLong()
        sut.state.value.onDeleteClick(id)

        val state = sut.state.value
        assertTrue(state.isRemoveBookmarkDialogVisible)
        assertEquals(id, state.bookmarkIdToDelete)
    }

    @Test
    fun `onDismissDeleteDialog hides dialog and clears bookmarkIdToDelete`() {
        createViewModel()

        sut.state.value.onDeleteClick(getRandomLong())
        sut.state.value.onDismissDeleteDialog()

        val state = sut.state.value
        assertFalse(state.isRemoveBookmarkDialogVisible)
        assertNull(state.bookmarkIdToDelete)
    }

    // --- confirm delete ---

    @Test
    fun `onConfirmDelete success emits onDeleteSuccess and hides dialog`() = runTest {
        val id = getRandomLong()
        createViewModel()

        sut.state.value.onDeleteClick(id)

        sut.onDeleteSuccess.test {
            sut.state.value.onConfirmDelete()

            awaitItem()
        }

        assertFalse(sut.state.value.isLoading)
        assertFalse(sut.state.value.isRemoveBookmarkDialogVisible)
        assertNull(sut.state.value.bookmarkIdToDelete)
        assertTrue(wikiRepository.deleteWikiLinkCalled)
        assertEquals(id, wikiRepository.deleteWikiLinkId)
    }

    @Test
    fun `onConfirmDelete failure shows snackbar and hides dialog`() = runTest {
        wikiRepository.deleteWikiLinkThrows = testException
        createViewModel()

        sut.state.value.onDeleteClick(getRandomLong())

        sut.snackBarMessage.test {
            sut.state.value.onConfirmDelete()

            val message = awaitItem()
            assertTrue(message !is NativeText.Empty)
        }

        assertFalse(sut.state.value.isLoading)
        assertTrue(wikiRepository.deleteWikiLinkCalled)
    }

    @Test
    fun `onConfirmDelete with no bookmarkIdToDelete does nothing`() {
        createViewModel()

        sut.state.value.onConfirmDelete()

        assertFalse(wikiRepository.deleteWikiLinkCalled)
    }

    // --- refresh ---

    @Test
    fun `refresh reloads bookmarks`() {
        val link = makeWikiLink()
        wikiRepository.getWikiLinksResult = persistentListOf(link)
        createViewModel()

        assertEquals(1, sut.state.value.bookmarks.size)

        val newLink = makeWikiLink()
        wikiRepository.getWikiLinksResult = persistentListOf(link, newLink)

        sut.state.value.refresh()

        assertEquals(2, sut.state.value.bookmarks.size)
    }
}
