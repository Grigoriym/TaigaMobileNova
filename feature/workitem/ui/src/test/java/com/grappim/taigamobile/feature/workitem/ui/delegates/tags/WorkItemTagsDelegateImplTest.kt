package com.grappim.taigamobile.feature.workitem.ui.delegates.tags

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.testException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemTagsDelegateImplTest {

    private lateinit var sut: WorkItemTagsDelegateImpl
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()

    @Before
    fun setup() {
        sut = WorkItemTagsDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator
        )
    }

    @Test
    fun `initial state should have empty tags and no loading`() {
        val state = sut.tagsState.value

        assertTrue(state.tags.isEmpty())
        assertFalse(state.areTagsLoading)
    }

    @Test
    fun `setInitialTags should update tags`() {
        val tags = persistentListOf(
            createTagUI("tag1"),
            createTagUI("tag2")
        )

        sut.setInitialTags(tags)

        assertEquals(tags, sut.tagsState.value.tags)
    }

    @Test
    fun `handleTagRemove should remove specified tag`() = runTest {
        val tag1 = createTagUI("tag1")
        val tag2 = createTagUI("tag2")
        val tag3 = createTagUI("tag3")
        sut.setInitialTags(persistentListOf(tag1, tag2, tag3))

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.handleTagRemove(
            tag = tag2,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val resultTags = sut.tagsState.value.tags
        assertEquals(2, resultTags.size)
        assertTrue(resultTags.any { it.name == "tag1" })
        assertTrue(resultTags.any { it.name == "tag3" })
        assertFalse(resultTags.any { it.name == "tag2" })
    }

    @Test
    fun `handleTagsUpdate should call doOnPreExecute`() = runTest {
        var preExecuteCalled = false
        val newTags = persistentListOf(createTagUI("tag1"))

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.handleTagsUpdate(
            newTags = newTags,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleTagsUpdate should update tags on success`() = runTest {
        val newTags = persistentListOf(
            createTagUI("newTag1"),
            createTagUI("newTag2")
        )

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.handleTagsUpdate(
            newTags = newTags,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.tagsState.value
        assertEquals(newTags, state.tags)
        assertFalse(state.areTagsLoading)
    }

    @Test
    fun `handleTagsUpdate should call doOnSuccess with new version`() = runTest {
        var receivedVersion: Long? = null
        val newVersion = getRandomLong()
        val newTags = persistentListOf(createTagUI("tag1"))

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)

        sut.handleTagsUpdate(
            newTags = newTags,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = { receivedVersion = it },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `handleTagsUpdate should clear loading on error`() = runTest {
        val newTags = persistentListOf(createTagUI("tag1"))

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleTagsUpdate(
            newTags = newTags,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.tagsState.value.areTagsLoading)
    }

    @Test
    fun `handleTagsUpdate should call doOnError on failure`() = runTest {
        var receivedError: Throwable? = null
        val newTags = persistentListOf(createTagUI("tag1"))

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleTagsUpdate(
            newTags = newTags,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    @Test
    fun `handleTagsUpdate should not update tags on error`() = runTest {
        val initialTags = persistentListOf(createTagUI("initialTag"))
        val newTags = persistentListOf(createTagUI("newTag"))
        sut.setInitialTags(initialTags)

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleTagsUpdate(
            newTags = newTags,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertEquals(initialTags, sut.tagsState.value.tags)
    }

    @Test
    fun `handleTagsUpdate should call repository with correct parameters`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val tag1 = TagUI(name = "tag1", color = Color.Red)
        val tag2 = TagUI(name = "tag2", color = Color.Blue)
        val newTags = persistentListOf(tag1, tag2)

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.handleTagsUpdate(
            newTags = newTags,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify {
            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = commonTaskType
            )
        }
    }

    @Test
    fun `handleTagRemove should call doOnSuccess with new version`() = runTest {
        var receivedVersion: Long? = null
        val newVersion = getRandomLong()
        val tag1 = createTagUI("tag1")
        val tag2 = createTagUI("tag2")
        sut.setInitialTags(persistentListOf(tag1, tag2))

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)

        sut.handleTagRemove(
            tag = tag1,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = { receivedVersion = it },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `handleTagRemove should call doOnError on failure`() = runTest {
        var receivedError: Throwable? = null
        val tag1 = createTagUI("tag1")
        sut.setInitialTags(persistentListOf(tag1))

        val payload = persistentMapOf<String, Any?>("tags" to listOf<List<String>>())
        every { patchDataGenerator.getTagsPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleTagRemove(
            tag = tag1,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    private fun createTagUI(name: String = getRandomString(), color: Color = Color.Red, isSelected: Boolean = false) =
        TagUI(
            name = name,
            color = color,
            isSelected = isSelected
        )
}
