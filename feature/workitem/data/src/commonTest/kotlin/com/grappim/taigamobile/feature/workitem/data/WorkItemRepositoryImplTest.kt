package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.GetWorkItemsParams
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributeResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customfield.CustomFieldTypeDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import com.grappim.taigamobile.feature.workitem.mapper.AttachmentMapper
import com.grappim.taigamobile.feature.workitem.mapper.CustomFieldsMapper
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.feature.workitem.mapper.JsonObjectMapper
import com.grappim.taigamobile.feature.workitem.mapper.PatchedDataMapper
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.FakeNetworkMonitor
import com.grappim.taigamobile.testing.api.FakeWorkItemApi
import com.grappim.taigamobile.testing.dao.FakeWorkItemDao
import com.grappim.taigamobile.testing.models.getAttachment
import com.grappim.taigamobile.testing.models.getAttachmentDTO
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDateTime
import io.ktor.utils.io.core.toByteArray
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemRepositoryImplTest {

    private val projectId = getRandomLong()

    private val fakeWorkItemApi = FakeWorkItemApi()
    private val patchedDataMapper = PatchedDataMapper(DueDateStatusMapper())
    private val attachmentMapper = AttachmentMapper()
    private val workItemMapper = WorkItemMapper(
        statusesMapper = StatusesMapper(),
        userMapper = UserMapper(),
        tagsMapper = TagsMapper(),
        projectMapper = ProjectMapper()
    )
    private val workItemEntityMapper = WorkItemEntityMapper(Json)
    private val fakeUsersRepository = FakeUsersRepository()
    private val customFieldsMapper = CustomFieldsMapper(FakeDateTimeUtils())
    private val fakeTaigaSessionStorage = FakeTaigaSessionStorage(currentProjectId = projectId)
    private val jsonObjectMapper = JsonObjectMapper()
    private val fakeWorkItemDao = FakeWorkItemDao()
    private val fakeNetworkMonitor = FakeNetworkMonitor(initialOnline = true)

    private lateinit var sut: WorkItemRepository

    @BeforeTest
    fun setup() {
        sut = WorkItemRepositoryImpl(
            workItemApi = fakeWorkItemApi,
            patchedDataMapper = patchedDataMapper,
            attachmentMapper = attachmentMapper,
            workItemMapper = workItemMapper,
            workItemEntityMapper = workItemEntityMapper,
            usersRepository = fakeUsersRepository,
            customFieldsMapper = customFieldsMapper,
            taigaSessionStorage = fakeTaigaSessionStorage,
            jsonObjectMapper = jsonObjectMapper,
            workItemDao = fakeWorkItemDao,
            networkMonitor = fakeNetworkMonitor
        )
    }

    @Test
    fun `getWorkItems should return mapped work items for user stories`() = runTest {
        val taskType = CommonTaskType.UserStory
        val dto1 = getWorkItemResponseDTO()
        val dto2 = getWorkItemResponseDTO()
        fakeWorkItemApi.workItemsResponse = listOf(dto1, dto2)

        val result = sut.getWorkItems(commonTaskType = taskType, projectId = projectId)

        assertEquals(2, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto2.id, result[1].id)
        assertEquals(1, fakeWorkItemApi.getWorkItemsCalls.size)
        val call = fakeWorkItemApi.getWorkItemsCalls.last()
        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(projectId, call.project)
    }

    @Test
    fun `getWorkItems should pass all filter parameters`() = runTest {
        val taskType = CommonTaskType.Issue
        val assignedId = getRandomLong()
        val watcherId = getRandomLong()
        val milestoneId = getRandomLong()
        val pageSize = 50
        val modifiedDateGte = "2024-01-01"
        val finishDateGte = "2024-12-31"
        fakeWorkItemApi.workItemsResponse = listOf(getWorkItemResponseDTO())

        sut.getWorkItems(
            commonTaskType = taskType,
            projectId = projectId,
            params = GetWorkItemsParams(
                assignedId = assignedId,
                isClosed = true,
                watcherId = watcherId,
                isDashboard = true,
                isBlocked = false,
                modifiedDateGte = modifiedDateGte,
                finishDateGte = finishDateGte,
                milestoneId = milestoneId,
                pageSize = pageSize
            )
        )

        val call = fakeWorkItemApi.getWorkItemsCalls.last()
        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(assignedId, call.assignedId)
        assertEquals(true, call.isClosed)
        assertEquals(watcherId, call.watcherId)
        assertEquals(true, call.isDashboard)
        assertEquals(false, call.isBlocked)
        assertEquals(modifiedDateGte, call.modifiedDateGte)
        assertEquals(finishDateGte, call.finishDateGte)
        assertEquals(milestoneId, call.sprint)
        assertEquals(pageSize, call.pageSize)
    }

    @Test
    fun `patchData should add version to payload and return patched data`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Task
        val payload = persistentMapOf<String, Any?>("subject" to "New Title")
        val responseDto = getWorkItemResponseDTO()
        fakeWorkItemApi.patchWorkItemResponse = responseDto

        val result = sut.patchData(
            version = version,
            workItemId = workItemId,
            payload = payload,
            commonTaskType = taskType
        )

        assertEquals(responseDto.version, result.newVersion)
        val call = fakeWorkItemApi.patchWorkItemCalls.last()
        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(workItemId, call.id)
        assertTrue(call.payload.containsKey("version"))
        assertTrue(call.payload.containsKey("subject"))
    }

    @Test
    fun `patchCustomAttributes should add version and return patched custom attributes`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.UserStory
        val responseDto = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = version + 1
        )
        fakeWorkItemApi.patchCustomAttributesValuesResponse = responseDto
        val payload = persistentMapOf<String, Any?>("attributes_values" to mapOf("1" to "value"))

        val result = sut.patchCustomAttributes(
            customAttributesVersion = version,
            workItemId = workItemId,
            payload = payload,
            commonTaskType = taskType
        )

        assertEquals(version + 1, result.version)
        val call = fakeWorkItemApi.patchCustomAttributesValuesCalls.last()
        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(workItemId, call.taskId)
        assertTrue(call.payload.containsKey("version"))
    }

    @Test
    fun `addAttachment should upload file and return mapped attachment`() = runTest {
        val workItemId = getRandomLong()
        val fileName = "test.png"
        val fileBytes = "test content".toByteArray()
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
        val attachmentDto = getAttachmentDTO()
        fakeWorkItemApi.uploadAttachmentResponse = attachmentDto

        val result = sut.addAttachment(
            workItemId = workItemId,
            fileName = fileName,
            fileByteArray = fileBytes,
            projectId = projectId,
            taskIdentifier = taskIdentifier
        )

        assertEquals(attachmentDto.id, result.id)
        assertEquals(attachmentDto.name, result.name)
        val call = fakeWorkItemApi.uploadAttachmentCalls.last()
        assertEquals("issues", call.taskPath)
        assertEquals(fileName, call.fileName)
        assertEquals(projectId, call.projectId)
        assertEquals(workItemId, call.objectId)
    }

    @Test
    fun `deleteAttachment should call api with correct parameters`() = runTest {
        val attachment = getAttachment()
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)

        sut.deleteAttachment(attachment, taskIdentifier)

        assertEquals(1, fakeWorkItemApi.deleteAttachmentCalls.size)
        val call = fakeWorkItemApi.deleteAttachmentCalls.last()
        assertEquals("tasks", call.first)
        assertEquals(attachment.id, call.second)
    }

    @Test
    fun `watchWorkItem should call api watch endpoint`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Epic

        sut.watchWorkItem(workItemId, taskType)

        assertEquals(1, fakeWorkItemApi.watchWorkItemCalls.size)
        val call = fakeWorkItemApi.watchWorkItemCalls.last()
        assertEquals(taskType.getPluralPath(), call.first)
        assertEquals(workItemId, call.second)
    }

    @Test
    fun `unwatchWorkItem should call api unwatch endpoint`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Issue

        sut.unwatchWorkItem(workItemId, taskType)

        assertEquals(1, fakeWorkItemApi.unwatchWorkItemCalls.size)
        val call = fakeWorkItemApi.unwatchWorkItemCalls.last()
        assertEquals(taskType.getPluralPath(), call.first)
        assertEquals(workItemId, call.second)
    }

    @Test
    fun `getUpdateWorkItem should return mapped update work item`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.UserStory
        val dto = getWorkItemResponseDTO()
        fakeWorkItemApi.workItemByIdResponse = dto

        val result = sut.getUpdateWorkItem(workItemId, taskType)

        assertEquals(dto.watchers?.toPersistentList() ?: persistentListOf(), result.watcherUserIds)
    }

    @Test
    fun `updateWatchersData should update watchers and return update data with watchers`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Task
        val newWatcherIds = persistentListOf(1L, 2L)
        val users = persistentListOf(getUser(), getUser())
        val patchedDto = getWorkItemResponseDTO()
        fakeWorkItemApi.patchWorkItemResponse = patchedDto
        fakeUsersRepository.getUsersListResult = users
        fakeUsersRepository.isAnyAssignedToMeResult = true

        val result = sut.updateWatchersData(
            version = version,
            workItemId = workItemId,
            newWatchers = newWatcherIds,
            commonTaskType = taskType
        )

        assertEquals(patchedDto.version, result.version)
        assertTrue(result.isWatchedByMe)
        assertEquals(users, result.watchers)
    }

    @Test
    fun `updateWatchersData should return empty watchers when newWatchers is empty`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Issue
        val newWatcherIds = persistentListOf<Long>()
        val patchedDto = getWorkItemResponseDTO()
        fakeWorkItemApi.patchWorkItemResponse = patchedDto

        val result = sut.updateWatchersData(
            version = version,
            workItemId = workItemId,
            newWatchers = newWatcherIds,
            commonTaskType = taskType
        )

        assertEquals(patchedDto.version, result.version)
        assertFalse(result.isWatchedByMe)
        assertTrue(result.watchers.isEmpty())
    }

    @Test
    fun `getCustomFields should fetch attributes and values concurrently`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.UserStory
        val attributes = listOf(
            CustomAttributeResponseDTO(
                id = 1L,
                name = "Field1",
                description = "Description",
                order = 1L,
                type = CustomFieldTypeDTO.Text,
                extra = null
            )
        )
        val values = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = 1L
        )
        fakeWorkItemApi.customAttributesResponse = attributes
        fakeWorkItemApi.customAttributesValuesResponse = values

        val result = sut.getCustomFields(workItemId, taskType)

        assertEquals(1, result.fields.size)
        assertEquals("Field1", result.fields.first().name)
        assertEquals(1L, result.version)
    }

    @Test
    fun `getWorkItemAttachments should return mapped attachments`() = runTest {
        val workItemId = getRandomLong()
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Epic)
        val attachmentDtos = listOf(getAttachmentDTO(), getAttachmentDTO())
        fakeWorkItemApi.getAttachmentsResponse = attachmentDtos

        val result = sut.getWorkItemAttachments(workItemId, taskIdentifier)

        assertEquals(2, result.size)
        assertEquals(attachmentDtos[0].id, result[0].id)
        assertEquals(attachmentDtos[1].id, result[1].id)
    }

    @Test
    fun `getWorkItemAttachments for wiki should use wiki path`() = runTest {
        val workItemId = getRandomLong()
        val taskIdentifier = TaskIdentifier.Wiki
        val attachmentDto = getAttachmentDTO()
        fakeWorkItemApi.getAttachmentsResponse = listOf(attachmentDto)

        val result = sut.getWorkItemAttachments(workItemId, taskIdentifier)

        assertEquals(1, result.size)
        assertEquals(attachmentDto.id, result.first().id)
    }

    @Test
    fun `deleteWorkItem should call api delete endpoint`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Task

        sut.deleteWorkItem(workItemId, taskType)

        assertEquals(1, fakeWorkItemApi.deleteWorkItemCalls.size)
        val call = fakeWorkItemApi.deleteWorkItemCalls.last()
        assertEquals(taskType.getPluralPath(), call.first)
        assertEquals(workItemId, call.second)
    }

    @Test
    fun `patchWikiPage should add version and return patched data`() = runTest {
        val pageId = getRandomLong()
        val version = getRandomLong()
        val payload = persistentMapOf<String, Any?>("content" to "New wiki content")
        val wikiPageDto = WikiPageDTO(
            id = pageId,
            projectId = projectId,
            slug = "test-page",
            content = "New wiki content",
            ownerId = 1L,
            lastModifierId = 1L,
            createdDate = nowLocalDateTime,
            modifiedDate = nowLocalDateTime,
            html = "<p>New wiki content</p>",
            editions = 2L,
            version = version + 1
        )
        fakeWorkItemApi.patchWikiPageResponse = wikiPageDto

        val result = sut.patchWikiPage(pageId, version, payload)

        assertEquals(version + 1, result.newVersion)
        val call = fakeWorkItemApi.patchWikiPageCalls.last()
        assertEquals(pageId, call.pageId)
        assertTrue(call.payload.containsKey("version"))
        assertTrue(call.payload.containsKey("content"))
    }

    @Test
    fun `createWorkItem should create work item and return mapped result`() = runTest {
        val taskType = CommonTaskType.Issue
        val subject = getRandomString()
        val description = getRandomString()
        val status = getRandomLong()
        val responseDto = getWorkItemResponseDTO()
        fakeWorkItemApi.createWorkItemResponse = responseDto

        val result = sut.createWorkItem(
            commonTaskType = taskType,
            subject = subject,
            description = description,
            status = status
        )

        assertEquals(responseDto.id, result.id)
        assertEquals(taskType, result.taskType)
        val call = fakeWorkItemApi.createWorkItemCalls.last()
        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(projectId, call.createRequest.project)
        assertEquals(subject, call.createRequest.subject)
        assertEquals(description, call.createRequest.description)
        assertEquals(status, call.createRequest.status)
    }

    @Test
    fun `createWorkItem should work without status`() = runTest {
        val taskType = CommonTaskType.UserStory
        val subject = getRandomString()
        val description = getRandomString()
        val responseDto = getWorkItemResponseDTO()
        fakeWorkItemApi.createWorkItemResponse = responseDto

        val result = sut.createWorkItem(
            commonTaskType = taskType,
            subject = subject,
            description = description,
            status = null
        )

        assertEquals(responseDto.id, result.id)
        val call = fakeWorkItemApi.createWorkItemCalls.last()
        assertEquals(null, call.createRequest.status)
    }

    @Test
    fun `promoteToUserStory should promote issue and return user story`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Issue
        val newUserStoryRef = getRandomLong()
        val userStoryDto = getWorkItemResponseDTO()
        fakeWorkItemApi.promoteToUserStoryResponse = listOf(newUserStoryRef)
        fakeWorkItemApi.workItemByRefResponse = userStoryDto

        val result = sut.promoteToUserStory(workItemId, taskType)

        assertEquals(userStoryDto.id, result.id)
        assertEquals(CommonTaskType.UserStory, result.taskType)
        val promoteCall = fakeWorkItemApi.promoteToUserStoryCalls.last()
        assertEquals(taskType.getPluralPath(), promoteCall.taskPath)
        assertEquals(workItemId, promoteCall.workItemId)
        assertEquals(projectId, promoteCall.body.projectId)
        val refCall = fakeWorkItemApi.workItemByRefCalls.last()
        assertEquals(CommonTaskType.UserStory.getPluralPath(), refCall.taskPath)
        assertEquals(newUserStoryRef, refCall.ref)
    }

    @Test
    fun `promoteToUserStory should promote task and return user story`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Task
        val newUserStoryRef = getRandomLong()
        val userStoryDto = getWorkItemResponseDTO()
        fakeWorkItemApi.promoteToUserStoryResponse = listOf(newUserStoryRef)
        fakeWorkItemApi.workItemByRefResponse = userStoryDto

        val result = sut.promoteToUserStory(workItemId, taskType)

        assertEquals(userStoryDto.id, result.id)
        assertEquals(CommonTaskType.UserStory, result.taskType)
    }

    @Test
    fun `promoteToUserStory should throw error for invalid task types`() = runTest {
        val workItemId = getRandomLong()

        assertFailsWith<IllegalStateException> {
            sut.promoteToUserStory(workItemId, CommonTaskType.UserStory)
        }

        assertFailsWith<IllegalStateException> {
            sut.promoteToUserStory(workItemId, CommonTaskType.Epic)
        }
    }

    @Test
    fun `promoteToUserStory should throw error when no user story ref returned`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Issue
        fakeWorkItemApi.promoteToUserStoryResponse = emptyList()

        assertFailsWith<IllegalStateException> {
            sut.promoteToUserStory(workItemId, taskType)
        }
    }

    @Test
    fun `getWorkItems should work for all task types`() = runTest {
        CommonTaskType.entries.forEach { taskType ->
            val dto = getWorkItemResponseDTO()
            fakeWorkItemApi.workItemsResponse = listOf(dto)
            fakeWorkItemApi.getWorkItemsCalls.clear()

            val result = sut.getWorkItems(commonTaskType = taskType, projectId = projectId)

            assertEquals(1, result.size)
            assertEquals(dto.id, result.first().id)
            assertEquals(taskType.getPluralPath(), fakeWorkItemApi.getWorkItemsCalls.last().taskPath)
        }
    }

    @Test
    fun `addAttachment for wiki should use wiki path`() = runTest {
        val workItemId = getRandomLong()
        val fileName = "document.pdf"
        val fileBytes = "pdf content".toByteArray()
        val taskIdentifier = TaskIdentifier.Wiki
        val attachmentDto = getAttachmentDTO()
        fakeWorkItemApi.uploadAttachmentResponse = attachmentDto

        val result = sut.addAttachment(
            workItemId = workItemId,
            fileName = fileName,
            fileByteArray = fileBytes,
            projectId = projectId,
            taskIdentifier = taskIdentifier
        )

        assertEquals(attachmentDto.id, result.id)
        val call = fakeWorkItemApi.uploadAttachmentCalls.last()
        assertEquals("wiki", call.taskPath)
    }

    @Test
    fun `deleteAttachment for wiki should use wiki path`() = runTest {
        val attachment = getAttachment()
        val taskIdentifier = TaskIdentifier.Wiki

        sut.deleteAttachment(attachment, taskIdentifier)

        val call = fakeWorkItemApi.deleteAttachmentCalls.last()
        assertEquals("wiki", call.first)
        assertEquals(attachment.id, call.second)
    }
}
