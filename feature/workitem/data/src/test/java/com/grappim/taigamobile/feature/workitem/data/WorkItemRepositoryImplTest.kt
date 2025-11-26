package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.api.CustomFieldsMapper
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.getAttachment
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getUser
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WorkItemRepositoryImplTest {

    private val workItemApi: WorkItemApi = mockk()
    private val patchedDataMapper: PatchedDataMapper = mockk()
    private val attachmentMapper: AttachmentMapper = mockk()
    private val workItemMapper: WorkItemMapper = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val customFieldsMapper: CustomFieldsMapper = mockk()
    private val taigaStorage: TaigaStorage = mockk()

    private lateinit var sut: WorkItemRepository

    @Before
    fun setup() {
        sut = WorkItemRepositoryImpl(
            workItemApi = workItemApi,
            patchedDataMapper = patchedDataMapper,
            attachmentMapper = attachmentMapper,
            workItemMapper = workItemMapper,
            usersRepository = usersRepository,
            customFieldsMapper = customFieldsMapper,
            taigaStorage = taigaStorage
        )
    }

    @Test
    fun `watchWorkItem should call workItemApi watchWorkItem`() = runTest {
        val workItemId = getRandomLong()
        val commonTaskType = CommonTaskType.Issue
        val taskPath = WorkItemPathPlural(commonTaskType)
        coEvery { workItemApi.watchWorkItem(workItemId = workItemId, taskPath = taskPath) } just Runs

        sut.watchWorkItem(workItemId, commonTaskType)

        coVerify { workItemApi.watchWorkItem(workItemId = workItemId, taskPath = taskPath) }
    }

    @Test
    fun `unwatchWorkItem should call workItemApi unwatchWorkItem`() = runTest {
        val workItemId = getRandomLong()
        val commonTaskType = CommonTaskType.Task
        val taskPath = WorkItemPathPlural(commonTaskType)
        coEvery { workItemApi.unwatchWorkItem(workItemId = workItemId, taskPath = taskPath) } just Runs

        sut.unwatchWorkItem(workItemId, commonTaskType)

        coVerify { workItemApi.unwatchWorkItem(workItemId = workItemId, taskPath = taskPath) }
    }

    @Test
    fun `deleteAttachment should call workItemApi deleteAttachment`() = runTest {
        val attachment = getAttachment()
        val commonTaskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathPlural(commonTaskType)
        coEvery {
            workItemApi.deleteAttachment(
                attachmentId = attachment.id,
                taskPath = taskPath
            )
        } just Runs

        sut.deleteAttachment(attachment, commonTaskType)

        coVerify { workItemApi.deleteAttachment(attachmentId = attachment.id, taskPath = taskPath) }
    }

    @Test
    fun `getWorkItem should return correct WorkItem`() = runTest {
        val workItemId = getRandomLong()
        val commonTaskType = CommonTaskType.Epic
        val taskPath = WorkItemPathPlural(commonTaskType)
        val watcherIds = persistentListOf(1L, 2L, 3L)
        val expectedWorkItem = WorkItem(watcherUserIds = watcherIds)
        val mockResponse = mockk<WorkItemResponseDTO>()

        coEvery {
            workItemApi.getWorkItemById(
                taskPath = taskPath,
                id = workItemId
            )
        } returns mockResponse
        coEvery { workItemMapper.toDomain(mockResponse) } returns expectedWorkItem

        val actual = sut.getWorkItem(workItemId, commonTaskType)

        assertEquals(expectedWorkItem, actual)
        coVerify { workItemApi.getWorkItemById(taskPath = taskPath, id = workItemId) }
        coVerify { workItemMapper.toDomain(mockResponse) }
    }

    @Test
    fun `patchData should return correct PatchedData`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val payload = mapOf<String, Any?>("subject" to "New Subject").toPersistentMap()
        val commonTaskType = CommonTaskType.Issue
        val taskPath = WorkItemPathPlural(commonTaskType)
        val expectedVersion = getRandomLong()
        val mockResponse = mockk<WorkItemResponseDTO>()
        val expectedPatchedData = PatchedData(newVersion = expectedVersion, dueDateStatus = DueDateStatus.Set)

        val editedPayload = payload.toPersistentMap().put("version", version)
        coEvery {
            workItemApi.patchWorkItem(
                taskPath = taskPath,
                id = workItemId,
                payload = editedPayload
            )
        } returns mockResponse
        coEvery { patchedDataMapper.toDomain(mockResponse) } returns expectedPatchedData

        val actual = sut.patchData(version, workItemId, payload, commonTaskType)

        assertEquals(expectedPatchedData, actual)
        coVerify {
            workItemApi.patchWorkItem(
                taskPath = taskPath,
                id = workItemId,
                payload = editedPayload
            )
        }
        coVerify { patchedDataMapper.toDomain(mockResponse) }
    }

    @Test
    fun `patchCustomAttributes should return correct PatchedCustomAttributes`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val payload = mapOf<String, Any?>("1" to "value1", "2" to "value2").toPersistentMap()
        val commonTaskType = CommonTaskType.Task
        val taskPath = WorkItemPathPlural(commonTaskType)
        val expectedVersion = getRandomLong()
        val mockResponse = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = expectedVersion
        )
        val expectedPatchedCustomAttributes = PatchedCustomAttributes(version = expectedVersion)

        val editedPayload = payload.toPersistentMap().put("version", version)
        coEvery {
            workItemApi.patchCustomAttributesValues(
                taskPath = taskPath,
                taskId = workItemId,
                payload = editedPayload
            )
        } returns mockResponse
        coEvery { patchedDataMapper.toDomainCustomAttrs(mockResponse) } returns expectedPatchedCustomAttributes

        val actual = sut.patchCustomAttributes(version, workItemId, payload, commonTaskType)

        assertEquals(expectedPatchedCustomAttributes, actual)
        coVerify {
            workItemApi.patchCustomAttributesValues(
                taskPath = taskPath,
                taskId = workItemId,
                payload = editedPayload
            )
        }
        coVerify { patchedDataMapper.toDomainCustomAttrs(mockResponse) }
    }

    @Test
    fun `updateWatchersData should return correct data when watchers list is not empty`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val watcherIds = persistentListOf(1L, 2L, 3L)
        val commonTaskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathPlural(commonTaskType)
        val newVersion = getRandomLong()
        val mockResponse = mockk<WorkItemResponseDTO>()
        val patchedData = PatchedData(newVersion = newVersion, dueDateStatus = null)
        val user1 = getUser().copy(id = 1L)
        val user2 = getUser().copy(id = 2L)
        val user3 = getUser().copy(id = 3L)
        val users = listOf(user1, user2, user3).toPersistentList()

        coEvery {
            workItemApi.patchWorkItem(
                taskPath = taskPath,
                id = workItemId,
                payload = any()
            )
        } returns mockResponse
        coEvery { patchedDataMapper.toDomain(mockResponse) } returns patchedData
        coEvery { usersRepository.getUsersList(watcherIds.toList()) } returns users
        coEvery { usersRepository.isAnyAssignedToMe(users) } returns true

        val actual = sut.updateWatchersData(version, workItemId, watcherIds, commonTaskType)

        assertEquals(newVersion, actual.version)
        assertTrue(actual.isWatchedByMe)
        assertEquals(users, actual.watchers)
    }

    @Test
    fun `updateWatchersData should return empty watchers when list is empty`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val watcherIds = persistentListOf<Long>()
        val commonTaskType = CommonTaskType.Epic
        val taskPath = WorkItemPathPlural(commonTaskType)
        val newVersion = getRandomLong()
        val mockResponse = mockk<WorkItemResponseDTO>()
        val patchedData = PatchedData(newVersion = newVersion, dueDateStatus = null)

        coEvery {
            workItemApi.patchWorkItem(
                taskPath = taskPath,
                id = workItemId,
                payload = any()
            )
        } returns mockResponse
        coEvery { patchedDataMapper.toDomain(mockResponse) } returns patchedData

        val actual = sut.updateWatchersData(version, workItemId, watcherIds, commonTaskType)

        assertEquals(newVersion, actual.version)
        assertFalse(actual.isWatchedByMe)
        assertEquals(persistentListOf(), actual.watchers)
    }
}
