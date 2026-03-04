package com.grappim.taigamobile.feature.wiki.ui.page.list

import app.cash.turbine.test
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeWikiRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDateTime
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

internal class WikiPagesViewModelTest {

    private val wikiRepository = FakeWikiRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: WikiPagesViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = WikiPagesViewModel(
            wikiRepository = wikiRepository,
            projectsRepository = projectsRepository
        )
    }

    private fun makeWikiPage(id: Long = getRandomLong(), slug: String = getRandomString()): WikiPage = WikiPage(
        id = id,
        version = 1L,
        content = "",
        editions = 0L,
        createdDate = nowLocalDateTime,
        isWatcher = false,
        lastModifier = null,
        modifiedDate = nowLocalDateTime,
        totalWatchers = 0L,
        slug = slug
    )

    // --- initial load ---

    @Test
    fun `on init success pages are loaded into state`() {
        val page = makeWikiPage()
        wikiRepository.getProjectWikiPagesResult = Result.success(persistentListOf(page))

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.allPages.size)
        assertEquals(page.id, state.allPages[0].id)
        assertEquals(page.slug, state.allPages[0].slug)
        assertEquals(page.slug, state.allPages[0].title)
    }

    @Test
    fun `on init failure state has error and is not loading`() = runTest {
        wikiRepository.getProjectWikiPagesResult = Result.failure(testException)

        createViewModel()

        // fetchPages() calls showSnackbarSuspend before setting error/isLoading=false;
        // consume the snackbar so the coroutine can finish updating state.
        sut.snackBarMessage.test {
            assertTrue(awaitItem() !is NativeText.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error !is NativeText.Empty)
        assertTrue(state.allPages.isEmpty())
    }

    // --- permissions ---

    @Test
    fun `canAddWikiPage is true when permission is present`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.ADD_WIKI_PAGE)

        createViewModel()

        assertTrue(sut.state.value.canAddWikiPage)
    }

    @Test
    fun `canAddWikiPage is false when permission is absent`() {
        projectsRepository.permissions = persistentListOf()

        createViewModel()

        assertFalse(sut.state.value.canAddWikiPage)
    }

    @Test
    fun `canDeleteWikiPage is true when permission is present`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.DELETE_WIKI_PAGE)

        createViewModel()

        assertTrue(sut.state.value.canDeleteWikiPage)
    }

    @Test
    fun `canDeleteWikiPage is false when permission is absent`() {
        projectsRepository.permissions = persistentListOf()

        createViewModel()

        assertFalse(sut.state.value.canDeleteWikiPage)
    }

    // --- delete dialog ---

    @Test
    fun `onDeleteClick shows dialog and sets pageIdToDelete`() {
        createViewModel()

        val id = getRandomLong()
        sut.state.value.onDeleteClick(id)

        val state = sut.state.value
        assertTrue(state.isRemovePageDialogVisible)
        assertEquals(id, state.pageIdToDelete)
    }

    @Test
    fun `onDismissDeleteDialog hides dialog and clears pageIdToDelete`() {
        createViewModel()

        sut.state.value.onDeleteClick(getRandomLong())
        sut.state.value.onDismissDeleteDialog()

        val state = sut.state.value
        assertFalse(state.isRemovePageDialogVisible)
        assertNull(state.pageIdToDelete)
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
        assertFalse(sut.state.value.isRemovePageDialogVisible)
        assertNull(sut.state.value.pageIdToDelete)
        assertTrue(wikiRepository.deleteWikiPageCalled)
        assertEquals(id, wikiRepository.deleteWikiPageId)
    }

    @Test
    fun `onConfirmDelete failure shows snackbar and hides dialog`() = runTest {
        wikiRepository.deleteWikiPageThrows = testException
        createViewModel()

        sut.state.value.onDeleteClick(getRandomLong())

        sut.snackBarMessage.test {
            sut.state.value.onConfirmDelete()

            val message = awaitItem()
            assertTrue(message !is NativeText.Empty)
        }

        assertFalse(sut.state.value.isLoading)
        assertTrue(wikiRepository.deleteWikiPageCalled)
    }

    @Test
    fun `onConfirmDelete with no pageIdToDelete does nothing`() {
        createViewModel()

        // pageIdToDelete is null, confirm should be a no-op
        sut.state.value.onConfirmDelete()

        assertFalse(wikiRepository.deleteWikiPageCalled)
    }

    // --- refresh ---

    @Test
    fun `refresh reloads pages`() {
        val page = makeWikiPage()
        wikiRepository.getProjectWikiPagesResult = Result.success(persistentListOf(page))
        createViewModel()

        assertEquals(1, sut.state.value.allPages.size)

        val newPage = makeWikiPage()
        wikiRepository.getProjectWikiPagesResult = Result.success(persistentListOf(page, newPage))

        sut.state.value.refresh()

        assertEquals(2, sut.state.value.allPages.size)
    }
}
