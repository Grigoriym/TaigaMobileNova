package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.feature.workitem.mapper.WikiLinkMapper
import com.grappim.taigamobile.feature.workitem.mapper.WikiPageMapper
import com.grappim.taigamobile.testing.api.FakeWikiApi
import com.grappim.taigamobile.testing.models.getWikiLinkDTO
import com.grappim.taigamobile.testing.models.getWikiPageDTO
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.assertFailsWithTestException
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WikiRepositoryImplTest {

    private val projectId = getRandomLong()

    private val fakeWikiApi = FakeWikiApi()
    private val fakeTaigaSessionStorage = FakeTaigaSessionStorage(currentProjectId = projectId)
    private val wikiPageMapper = WikiPageMapper()
    private val wikiLinkMapper = WikiLinkMapper()

    private lateinit var sut: WikiRepository

    @BeforeTest
    fun setup() {
        sut = WikiRepositoryImpl(
            wikiApi = fakeWikiApi,
            taigaSessionStorage = fakeTaigaSessionStorage,
            wikiPageMapper = wikiPageMapper,
            wikiLinkMapper = wikiLinkMapper
        )
    }

    @Test
    fun `getProjectWikiPages should pass current project id and map the response`() = runTest {
        val dto1 = getWikiPageDTO()
        val dto2 = getWikiPageDTO()
        fakeWikiApi.wikiPagesResult = listOf(dto1, dto2)

        val result = sut.getProjectWikiPages()

        assertEquals(projectId, fakeWikiApi.lastGetProjectWikiPagesProjectId)
        assertEquals(2, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto1.slug, result[0].slug)
        assertEquals(dto2.id, result[1].id)
        assertEquals(dto2.slug, result[1].slug)
    }

    @Test
    fun `getProjectWikiPages should return empty list when api returns nothing`() = runTest {
        fakeWikiApi.wikiPagesResult = emptyList()

        val result = sut.getProjectWikiPages()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getProjectWikiPages should propagate api error`() = runTest {
        fakeWikiApi.errorToThrow = testException

        assertFailsWithTestException { sut.getProjectWikiPages() }
    }

    @Test
    fun `getProjectWikiPageBySlug should pass current project id and slug and map the response`() = runTest {
        val slug = getRandomString()
        val dto = getWikiPageDTO()
        fakeWikiApi.wikiPageBySlugResult = dto

        val result = sut.getProjectWikiPageBySlug(slug)

        assertEquals(projectId, fakeWikiApi.lastGetBySlugProjectId)
        assertEquals(slug, fakeWikiApi.lastGetBySlugSlug)
        assertEquals(dto.id, result.id)
        assertEquals(dto.slug, result.slug)
        assertEquals(dto.content, result.content)
        assertEquals(dto.version, result.version)
    }

    @Test
    fun `getProjectWikiPageBySlug should propagate api error`() = runTest {
        fakeWikiApi.errorToThrow = testException

        assertFailsWithTestException { sut.getProjectWikiPageBySlug(getRandomString()) }
    }

    @Test
    fun `deleteWikiPage should pass the page id`() = runTest {
        val pageId = getRandomLong()

        sut.deleteWikiPage(pageId)

        assertEquals(listOf(pageId), fakeWikiApi.deletedPageIds)
    }

    @Test
    fun `deleteWikiPage should propagate api error`() = runTest {
        fakeWikiApi.errorToThrow = testException

        assertFailsWithTestException { sut.deleteWikiPage(getRandomLong()) }
    }

    @Test
    fun `getWikiLinks should pass current project id and map the response`() = runTest {
        val dto1 = getWikiLinkDTO()
        val dto2 = getWikiLinkDTO()
        fakeWikiApi.wikiLinksResult = listOf(dto1, dto2)

        val result = sut.getWikiLinks()

        assertEquals(projectId, fakeWikiApi.lastGetWikiLinkProjectId)
        assertEquals(2, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto1.href, result[0].ref)
        assertEquals(dto1.title, result[0].title)
        assertEquals(dto2.id, result[1].id)
        assertEquals(dto2.href, result[1].ref)
    }

    @Test
    fun `getWikiLinks should return empty list when api returns nothing`() = runTest {
        fakeWikiApi.wikiLinksResult = emptyList()

        val result = sut.getWikiLinks()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getWikiLinks should propagate api error`() = runTest {
        fakeWikiApi.errorToThrow = testException

        assertFailsWithTestException { sut.getWikiLinks() }
    }

    @Test
    fun `createWikiLink should build the request with current project id and map the response`() = runTest {
        val title = getRandomString()
        val dto = getWikiLinkDTO()
        fakeWikiApi.createWikiLinkResult = dto

        val result = sut.createWikiLink(title)

        assertEquals(
            NewWikiLinkRequestDTO(project = projectId, title = title),
            fakeWikiApi.lastCreateWikiLinkRequest
        )
        assertEquals(dto.id, result.id)
        assertEquals(dto.title, result.title)
        assertEquals(dto.href, result.ref)
        assertEquals(dto.order, result.order)
    }

    @Test
    fun `createWikiLink should propagate api error`() = runTest {
        fakeWikiApi.errorToThrow = testException

        assertFailsWithTestException { sut.createWikiLink(getRandomString()) }
    }

    @Test
    fun `deleteWikiLink should pass the link id`() = runTest {
        val linkId = getRandomLong()

        sut.deleteWikiLink(linkId)

        assertEquals(listOf(linkId), fakeWikiApi.deletedLinkIds)
    }

    @Test
    fun `deleteWikiLink should propagate api error`() = runTest {
        fakeWikiApi.errorToThrow = testException

        assertFailsWithTestException { sut.deleteWikiLink(getRandomLong()) }
    }

    @Test
    fun `createWikiPage should build the request with current project id and map the response`() = runTest {
        val slug = getRandomString()
        val content = getRandomString()
        val dto = getWikiPageDTO()
        fakeWikiApi.createWikiPageResult = dto

        val result = sut.createWikiPage(slug = slug, content = content)

        assertEquals(
            CreateWikiPageRequestDTO(projectId = projectId, slug = slug, content = content),
            fakeWikiApi.lastCreateWikiPageRequest
        )
        assertEquals(dto.id, result.id)
        assertEquals(dto.slug, result.slug)
        assertEquals(dto.content, result.content)
    }

    @Test
    fun `createWikiPage should propagate api error`() = runTest {
        fakeWikiApi.errorToThrow = testException

        assertFailsWithTestException {
            sut.createWikiPage(slug = getRandomString(), content = getRandomString())
        }
    }

    @Test
    fun `all methods should read the project id from session storage at call time`() = runTest {
        val newProjectId = getRandomLong()
        fakeTaigaSessionStorage.setCurrentProjectId(newProjectId)
        fakeWikiApi.createWikiPageResult = getWikiPageDTO()
        fakeWikiApi.wikiPageBySlugResult = getWikiPageDTO()

        sut.getProjectWikiPages()
        sut.getProjectWikiPageBySlug(getRandomString())
        sut.getWikiLinks()
        sut.createWikiPage(slug = getRandomString(), content = getRandomString())

        assertEquals(newProjectId, fakeWikiApi.lastGetProjectWikiPagesProjectId)
        assertEquals(newProjectId, fakeWikiApi.lastGetBySlugProjectId)
        assertEquals(newProjectId, fakeWikiApi.lastGetWikiLinkProjectId)
        assertEquals(newProjectId, fakeWikiApi.lastCreateWikiPageRequest?.projectId)
    }
}
