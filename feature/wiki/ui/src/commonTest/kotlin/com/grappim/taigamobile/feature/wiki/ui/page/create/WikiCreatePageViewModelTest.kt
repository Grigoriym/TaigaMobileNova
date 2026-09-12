package com.grappim.taigamobile.feature.wiki.ui.page.create

import androidx.compose.ui.text.input.TextFieldValue
import app.cash.turbine.test
import com.grappim.kit.testing.MainDispatcherRule
import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWikiRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDateTime
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WikiCreatePageViewModelTest {

    private val wikiRepository = FakeWikiRepository()
    private val usersRepository = FakeUsersRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: WikiCreatePageViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = WikiCreatePageViewModel(wikiRepository = wikiRepository, usersRepository = usersRepository)
    }

    private fun makeWikiPage(): WikiPage = WikiPage(
        id = getRandomLong(),
        version = 1L,
        content = getRandomString(),
        editions = 0L,
        createdDate = nowLocalDateTime,
        isWatcher = false,
        lastModifier = null,
        modifiedDate = nowLocalDateTime,
        totalWatchers = 0L,
        slug = getRandomString()
    )

    // --- initial state ---

    @Test
    fun `initial state has empty slug and content`() {
        createViewModel()

        val state = sut.state.value
        assertEquals("", state.slug)
        assertEquals(TextFieldValue(), state.content)
        assertFalse(state.isLoading)
        assertTrue(state.error is NativeText.Empty)
    }

    // --- init ---

    @Test
    fun `on init members are loaded via the mentions delegate`() {
        val members = persistentListOf(
            TeamMember(id = 1L, avatarUrl = null, name = "Alice Anderson", role = "Developer", username = "alice")
        )
        usersRepository.getTeamMembersResult = members

        createViewModel()

        assertEquals(members, sut.mentionsState.value.members)
    }

    // --- setSlug ---

    @Test
    fun `setSlug updates slug in state`() {
        createViewModel()

        val slug = getRandomString()
        sut.state.value.setSlug(slug)

        assertEquals(slug, sut.state.value.slug)
    }

    @Test
    fun `setSlug clears existing error`() {
        wikiRepository.createWikiPageThrows = testException
        createViewModel()

        sut.state.value.setSlug(getRandomString())
        sut.state.value.onCreateWikiPage()
        // error is set after failure
        sut.state.value.setSlug(getRandomString())

        assertTrue(sut.state.value.error is NativeText.Empty)
    }

    // --- setContent ---

    @Test
    fun `setContent updates content in state`() {
        createViewModel()

        val content = TextFieldValue(getRandomString())
        sut.state.value.setContent(content)

        assertEquals(content, sut.state.value.content)
    }

    // --- createWikiPage success ---

    @Test
    fun `createWikiPage success emits result and clears loading`() = runTest {
        val page = makeWikiPage()
        wikiRepository.createWikiPageResult = page
        createViewModel()

        val slug = getRandomString()
        val content = getRandomString()
        sut.state.value.setSlug(slug)
        sut.state.value.setContent(TextFieldValue(content))

        sut.creationResult.test {
            sut.state.value.onCreateWikiPage()

            val emitted = awaitItem()
            assertEquals(page.id, emitted.id)
            assertEquals(page.slug, emitted.slug)
        }

        assertFalse(sut.state.value.isLoading)
        assertTrue(sut.state.value.error is NativeText.Empty)
        assertTrue(wikiRepository.createWikiPageCalled)
        assertEquals(slug, wikiRepository.createWikiPageSlug)
        assertEquals(content, wikiRepository.createWikiPageContent)
    }

    // --- createWikiPage failure ---

    @Test
    fun `createWikiPage failure sets error and clears loading`() = runTest {
        wikiRepository.createWikiPageThrows = testException
        createViewModel()

        sut.state.value.setSlug(getRandomString())
        sut.state.value.onCreateWikiPage()

        assertFalse(sut.state.value.isLoading)
        assertTrue(sut.state.value.error !is NativeText.Empty)
        assertTrue(wikiRepository.createWikiPageCalled)
    }
}
