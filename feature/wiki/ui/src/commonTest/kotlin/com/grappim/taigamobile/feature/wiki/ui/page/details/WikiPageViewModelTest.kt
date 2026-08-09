package com.grappim.taigamobile.feature.wiki.ui.page.details

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.wiki.domain.WikiPageUseCase
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getAttachment
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWikiRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
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

internal class WikiPageViewModelTest {

    private val wikiId = getRandomLong()
    private val slug = getRandomString()

    private val savedStateHandle = SavedStateHandle(
        mapOf("slug" to slug, "id" to wikiId)
    )

    private val wikiRepository = FakeWikiRepository()
    private val usersRepository = FakeUsersRepository()
    private val workItemRepository = FakeWorkItemRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val taigaSessionStorage = FakeTaigaSessionStorage()
    private val patchDataGenerator = PatchDataGeneratorImpl()
    private val workItemEditStateRepository = WorkItemEditStateRepository()

    private val wikiPageUseCase = WikiPageUseCase(
        wikiRepository = wikiRepository,
        usersRepository = usersRepository,
        workItemRepository = workItemRepository,
        projectsRepository = projectsRepository
    )

    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: WikiPageViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
        wikiRepository.getProjectWikiPageBySlugResult = makeWikiPage()
        wikiRepository.getWikiLinksResult = persistentListOf()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = WikiPageViewModel(
            wikiPageUseCase = wikiPageUseCase,
            workItemRepository = workItemRepository,
            taigaSessionStorage = taigaSessionStorage,
            patchDataGenerator = patchDataGenerator,
            workItemEditStateRepository = workItemEditStateRepository,
            savedStateHandle = savedStateHandle
        )
    }

    private fun makeWikiPage(id: Long = wikiId, version: Long = 1L, content: String = getRandomString()): WikiPage =
        WikiPage(
            id = id,
            version = version,
            content = content,
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
    fun `on init success state has page data and is not loading`() {
        val page = makeWikiPage()
        wikiRepository.getProjectWikiPageBySlugResult = page

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.error)
        assertEquals(page, state.currentPage)
        assertEquals(page, state.originalPage)
    }

    @Test
    fun `on init success canModifyPage and shouldShowActions reflect permission`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.MODIFY_WIKI_PAGE)

        createViewModel()

        assertTrue(sut.state.value.canModifyPage)
        assertTrue(sut.state.value.shouldShowActions)
    }

    @Test
    fun `on init success canModifyPage is false when permission absent`() {
        projectsRepository.permissions = persistentListOf()

        createViewModel()

        assertFalse(sut.state.value.canModifyPage)
        assertFalse(sut.state.value.shouldShowActions)
    }

    @Test
    fun `on init failure state has error and is not loading`() {
        wikiRepository.getProjectWikiPageBySlugThrows = testException

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error !is NativeText.Empty)
        assertNull(state.currentPage)
    }

    @Test
    fun `on init wikiLink is set when matching link exists`() {
        val page = makeWikiPage()
        val link = com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink(
            ref = slug,
            id = getRandomLong(),
            order = 1L,
            title = getRandomString()
        )
        wikiRepository.getProjectWikiPageBySlugResult = page
        wikiRepository.getWikiLinksResult = persistentListOf(link)

        createViewModel()

        assertEquals(link, sut.state.value.link)
    }

    // --- delete alert ---

    @Test
    fun `setDeleteAlertVisible true shows delete alert`() {
        createViewModel()

        assertFalse(sut.state.value.isDeleteAlertVisible)

        sut.state.value.setDeleteAlertVisible(true)

        assertTrue(sut.state.value.isDeleteAlertVisible)
    }

    @Test
    fun `setDeleteAlertVisible false hides delete alert`() {
        createViewModel()

        sut.state.value.setDeleteAlertVisible(true)
        sut.state.value.setDeleteAlertVisible(false)

        assertFalse(sut.state.value.isDeleteAlertVisible)
    }

    // --- dropdown menu ---

    @Test
    fun `setDropdownMenuExpanded true expands dropdown`() {
        createViewModel()

        assertFalse(sut.state.value.isDropdownMenuExpanded)

        sut.state.value.setDropdownMenuExpanded(true)

        assertTrue(sut.state.value.isDropdownMenuExpanded)
    }

    @Test
    fun `setDropdownMenuExpanded false collapses dropdown`() {
        createViewModel()

        sut.state.value.setDropdownMenuExpanded(true)
        sut.state.value.setDropdownMenuExpanded(false)

        assertFalse(sut.state.value.isDropdownMenuExpanded)
    }

    // --- delete wiki page ---

    @Test
    fun `deleteWikiPage success emits deleteWikiPageResult`() = runTest {
        createViewModel()

        sut.deleteWikiPageResult.test {
            sut.state.value.onDeleteConfirm()
            awaitItem()
        }

        assertFalse(sut.state.value.isLoading)
        assertTrue(wikiRepository.deleteWikiPageCalled)
        assertEquals(wikiId, wikiRepository.deleteWikiPageId)
    }

    @Test
    fun `deleteWikiPage success with link also deletes wiki link`() = runTest {
        val link = com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink(
            ref = slug,
            id = getRandomLong(),
            order = 1L,
            title = getRandomString()
        )
        wikiRepository.getWikiLinksResult = persistentListOf(link)
        createViewModel()

        sut.deleteWikiPageResult.test {
            sut.state.value.onDeleteConfirm()
            awaitItem()
        }

        assertTrue(wikiRepository.deleteWikiLinkCalled)
        assertEquals(link.id, wikiRepository.deleteWikiLinkId)
    }

    @Test
    fun `deleteWikiPage failure updates state with error`() {
        wikiRepository.deleteWikiPageThrows = testException
        createViewModel()

        sut.state.value.onDeleteConfirm()

        assertFalse(sut.state.value.isLoading)
        assertTrue(sut.state.value.error !is NativeText.Empty)
    }

    // --- onAttachmentRemove ---

    @Test
    fun `onAttachmentRemove success removes attachment from attachmentsState`() = runTest {
        val attachment = getAttachment()
        workItemRepository.getWorkItemAttachmentsResult = persistentListOf(attachment)
        createViewModel()
        sut.setInitialAttachments(listOf(attachment))

        sut.state.value.onAttachmentRemove(attachment)

        assertFalse(sut.attachmentsState.value.attachments.contains(attachment))
        assertEquals(NativeText.Empty, sut.state.value.error)
    }

    @Test
    fun `onAttachmentRemove failure updates state with error`() {
        val attachment = getAttachment()
        workItemRepository.deleteAttachmentThrows = testException
        createViewModel()
        sut.setInitialAttachments(listOf(attachment))

        sut.state.value.onAttachmentRemove(attachment)

        assertTrue(sut.state.value.error !is NativeText.Empty)
    }

    // --- onNewDescriptionUpdate (via workItemEditStateRepository.updateDescription) ---

    @Test
    fun `description update success patches wiki page and updates currentPage`() = runTest {
        val page = makeWikiPage(content = "original")
        wikiRepository.getProjectWikiPageBySlugResult = page
        val newVersion = getRandomLong()
        workItemRepository.patchWikiPageResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        createViewModel()

        workItemEditStateRepository.updateDescription(wikiId, TaskIdentifier.Wiki, "updated")

        assertEquals("updated", sut.state.value.currentPage?.content)
        assertEquals("updated", sut.state.value.originalPage?.content)
        assertEquals(newVersion, sut.state.value.currentPage?.version)
        assertEquals(NativeText.Empty, sut.state.value.error)
    }

    @Test
    fun `description update failure updates state with error`() = runTest {
        val page = makeWikiPage(content = "original")
        wikiRepository.getProjectWikiPageBySlugResult = page
        workItemRepository.patchWikiPageThrows = testException
        createViewModel()

        workItemEditStateRepository.updateDescription(wikiId, TaskIdentifier.Wiki, "updated")

        assertTrue(sut.state.value.error !is NativeText.Empty)
        assertEquals("original", sut.state.value.currentPage?.content)
    }

    @Test
    fun `description update with unchanged content does not call patchWikiPage`() = runTest {
        val page = makeWikiPage(content = "same")
        wikiRepository.getProjectWikiPageBySlugResult = page
        createViewModel()

        workItemEditStateRepository.updateDescription(wikiId, TaskIdentifier.Wiki, "same")

        assertTrue(workItemRepository.patchWikiPageCalls.isEmpty())
    }
}
