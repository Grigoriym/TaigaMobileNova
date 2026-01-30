package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchedCustomAttributes
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.UpdateWorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathSingular
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomField
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFieldType
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.dto.CreateWorkItemRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.PromoteToUserStoryRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributeResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customfield.CustomFieldTypeDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import com.grappim.taigamobile.feature.workitem.mapper.AttachmentMapper
import com.grappim.taigamobile.feature.workitem.mapper.CustomFieldsMapper
import com.grappim.taigamobile.feature.workitem.mapper.JsonObjectMapper
import com.grappim.taigamobile.feature.workitem.mapper.PatchedDataMapper
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.getAttachment
import com.grappim.taigamobile.testing.getAttachmentDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getUser
import com.grappim.taigamobile.testing.getWorkItem
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemRepositoryImplTest {

    private val workItemApi: WorkItemApi = mockk()
    private val patchedDataMapper: PatchedDataMapper = mockk()
    private val attachmentMapper: AttachmentMapper = mockk()
    private val workItemMapper: WorkItemMapper = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val customFieldsMapper: CustomFieldsMapper = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val jsonObjectMapper: JsonObjectMapper = mockk()

    private lateinit var sut: WorkItemRepository

    private val projectId = getRandomLong()

    @Before
    fun setup() {
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        every { jsonObjectMapper.fromMapToJsonObject(any()) } answers {
            val map = firstArg<Map<String, Any?>>()
            JsonObjectMapper().fromMapToJsonObject(map)
        }

        sut = WorkItemRepositoryImpl(
            workItemApi = workItemApi,
            patchedDataMapper = patchedDataMapper,
            attachmentMapper = attachmentMapper,
            workItemMapper = workItemMapper,
            usersRepository = usersRepository,
            customFieldsMapper = customFieldsMapper,
            taigaSessionStorage = taigaSessionStorage,
            jsonObjectMapper = jsonObjectMapper
        )
    }

    @Test
    fun `getWorkItems should return mapped work items for user stories`() = runTest {
        val taskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathPlural(taskType)
        val dtos = listOf(getWorkItemResponseDTO(), getWorkItemResponseDTO())
        val expectedItems = persistentListOf(
            getWorkItem(taskType = taskType),
            getWorkItem(taskType = taskType)
        )

        coEvery {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                assignedId = null,
                isClosed = null,
                watcherId = null,
                isDashboard = null,
                isBlocked = null,
                modifiedDateGte = null,
                finishDateGte = null,
                sprint = null,
                pageSize = null
            )
        } returns dtos
        coEvery { workItemMapper.toDomainList(dtos, taskType) } returns expectedItems

        val result = sut.getWorkItems(commonTaskType = taskType, projectId = projectId)

        assertEquals(expectedItems, result)
        coVerify { workItemApi.getWorkItems(taskPath = taskPath, project = projectId) }
    }

    @Test
    fun `getWorkItems should pass all filter parameters`() = runTest {
        val taskType = CommonTaskType.Issue
        val taskPath = WorkItemPathPlural(taskType)
        val assignedId = getRandomLong()
        val watcherId = getRandomLong()
        val milestoneId = getRandomLong()
        val pageSize = 50
        val modifiedDateGte = "2024-01-01"
        val finishDateGte = "2024-12-31"
        val dtos = listOf(getWorkItemResponseDTO())
        val expectedItems = persistentListOf(getWorkItem(taskType = taskType))

        coEvery {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                assignedId = assignedId,
                isClosed = true,
                watcherId = watcherId,
                isDashboard = true,
                isBlocked = false,
                modifiedDateGte = modifiedDateGte,
                finishDateGte = finishDateGte,
                sprint = milestoneId,
                pageSize = pageSize
            )
        } returns dtos
        coEvery { workItemMapper.toDomainList(dtos, taskType) } returns expectedItems

        val result = sut.getWorkItems(
            commonTaskType = taskType,
            projectId = projectId,
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

        assertEquals(expectedItems, result)
    }

    @Test
    fun `patchData should add version to payload and return patched data`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Task
        val taskPath = WorkItemPathPlural(taskType)
        val payload = persistentMapOf<String, Any?>("subject" to "New Title")
        val responseDto = getWorkItemResponseDTO()
        val expectedPatchedData = PatchedData(newVersion = version + 1, dueDateStatus = null)

        val jsonSlot = slot<JsonObject>()
        coEvery {
            workItemApi.patchWorkItem(
                taskPath = taskPath,
                id = workItemId,
                payload = capture(jsonSlot)
            )
        } returns responseDto
        every { patchedDataMapper.toDomain(responseDto) } returns expectedPatchedData

        val result = sut.patchData(
            version = version,
            workItemId = workItemId,
            payload = payload,
            commonTaskType = taskType
        )

        assertEquals(expectedPatchedData, result)
        assertTrue(jsonSlot.captured.containsKey("version"))
        assertTrue(jsonSlot.captured.containsKey("subject"))
    }

    @Test
    fun `patchCustomAttributes should add version and return patched custom attributes`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathPlural(taskType)
        val payload = persistentMapOf<String, Any?>("attributes_values" to mapOf("1" to "value"))
        val responseDto = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = version + 1
        )
        val expectedResult = PatchedCustomAttributes(version = version + 1)

        val jsonSlot = slot<JsonObject>()
        coEvery {
            workItemApi.patchCustomAttributesValues(
                taskPath = taskPath,
                taskId = workItemId,
                payload = capture(jsonSlot)
            )
        } returns responseDto
        every { patchedDataMapper.toDomainCustomAttrs(responseDto) } returns expectedResult

        val result = sut.patchCustomAttributes(
            customAttributesVersion = version,
            workItemId = workItemId,
            payload = payload,
            commonTaskType = taskType
        )

        assertEquals(expectedResult, result)
        assertTrue(jsonSlot.captured.containsKey("version"))
    }

    @Test
    fun `addAttachment should upload file and return mapped attachment`() = runTest {
        val workItemId = getRandomLong()
        val fileName = "test.png"
        val fileBytes = "test content".toByteArray()
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
        val attachmentDto = getAttachmentDTO()
        val expectedAttachment = getAttachment()

        coEvery {
            workItemApi.uploadCommonTaskAttachment(
                taskPath = "issues",
                file = any(),
                project = any(),
                objectId = any()
            )
        } returns attachmentDto
        every { attachmentMapper.toDomain(attachmentDto) } returns expectedAttachment

        val result = sut.addAttachment(
            workItemId = workItemId,
            fileName = fileName,
            fileByteArray = fileBytes,
            projectId = projectId,
            taskIdentifier = taskIdentifier
        )

        assertEquals(expectedAttachment, result)
        coVerify {
            workItemApi.uploadCommonTaskAttachment(
                taskPath = "issues",
                file = any<MultipartBody.Part>(),
                project = any<MultipartBody.Part>(),
                objectId = any<MultipartBody.Part>()
            )
        }
    }

    @Test
    fun `deleteAttachment should call api with correct parameters`() = runTest {
        val attachment = getAttachment()
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)

        coJustRun {
            workItemApi.deleteAttachment(
                taskPath = "tasks",
                attachmentId = attachment.id
            )
        }

        sut.deleteAttachment(attachment, taskIdentifier)

        coVerify {
            workItemApi.deleteAttachment(
                taskPath = "tasks",
                attachmentId = attachment.id
            )
        }
    }

    @Test
    fun `watchWorkItem should call api watch endpoint`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Epic
        val taskPath = WorkItemPathPlural(taskType)

        coJustRun { workItemApi.watchWorkItem(taskPath = taskPath, workItemId = workItemId) }

        sut.watchWorkItem(workItemId, taskType)

        coVerify { workItemApi.watchWorkItem(taskPath = taskPath, workItemId = workItemId) }
    }

    @Test
    fun `unwatchWorkItem should call api unwatch endpoint`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Issue
        val taskPath = WorkItemPathPlural(taskType)

        coJustRun { workItemApi.unwatchWorkItem(taskPath = taskPath, workItemId = workItemId) }

        sut.unwatchWorkItem(workItemId, taskType)

        coVerify { workItemApi.unwatchWorkItem(taskPath = taskPath, workItemId = workItemId) }
    }

    @Test
    fun `getUpdateWorkItem should return mapped update work item`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathPlural(taskType)
        val dto = getWorkItemResponseDTO()
        val expectedUpdateWorkItem = UpdateWorkItem(
            watcherUserIds = persistentListOf(1L, 2L, 3L)
        )

        coEvery { workItemApi.getWorkItemById(taskPath = taskPath, id = workItemId) } returns dto
        coEvery { workItemMapper.toUpdateDomain(dto) } returns expectedUpdateWorkItem

        val result = sut.getUpdateWorkItem(workItemId, taskType)

        assertEquals(expectedUpdateWorkItem, result)
    }

    @Test
    fun `updateWatchersData should update watchers and return update data with watchers`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Task
        val newWatcherIds = persistentListOf(1L, 2L)
        val users = persistentListOf(getUser(), getUser())
        val patchedDto = getWorkItemResponseDTO()
        val patchedData = PatchedData(newVersion = version + 1, dueDateStatus = null)

        coEvery {
            workItemApi.patchWorkItem(
                taskPath = WorkItemPathPlural(taskType),
                id = workItemId,
                payload = any()
            )
        } returns patchedDto
        every { patchedDataMapper.toDomain(patchedDto) } returns patchedData
        coEvery { usersRepository.getUsersList(newWatcherIds.toList()) } returns users
        coEvery { usersRepository.isAnyAssignedToMe(users) } returns true

        val result = sut.updateWatchersData(
            version = version,
            workItemId = workItemId,
            newWatchers = newWatcherIds,
            commonTaskType = taskType
        )

        assertEquals(patchedData.newVersion, result.version)
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
        val patchedData = PatchedData(newVersion = version + 1, dueDateStatus = null)

        coEvery {
            workItemApi.patchWorkItem(
                taskPath = WorkItemPathPlural(taskType),
                id = workItemId,
                payload = any()
            )
        } returns patchedDto
        every { patchedDataMapper.toDomain(patchedDto) } returns patchedData

        val result = sut.updateWatchersData(
            version = version,
            workItemId = workItemId,
            newWatchers = newWatcherIds,
            commonTaskType = taskType
        )

        assertEquals(patchedData.newVersion, result.version)
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
        val expectedCustomFields = CustomFields(
            fields = persistentListOf(
                CustomField(
                    id = 1L,
                    type = CustomFieldType.Text,
                    name = "Field1",
                    description = "Description",
                    value = null,
                    options = null
                )
            ),
            version = 1L
        )

        coEvery {
            workItemApi.getCustomAttributes(
                taskPath = WorkItemPathSingular(taskType),
                projectId = projectId
            )
        } returns attributes
        coEvery {
            workItemApi.getCustomAttributesValues(
                id = workItemId,
                taskPath = WorkItemPathPlural(taskType)
            )
        } returns values
        every { customFieldsMapper.toDomain(attributes, values) } returns expectedCustomFields

        val result = sut.getCustomFields(workItemId, taskType)

        assertEquals(expectedCustomFields, result)
        coVerify { workItemApi.getCustomAttributes(WorkItemPathSingular(taskType), projectId) }
        coVerify { workItemApi.getCustomAttributesValues(WorkItemPathPlural(taskType), workItemId) }
    }

    @Test
    fun `getWorkItemAttachments should return mapped attachments`() = runTest {
        val workItemId = getRandomLong()
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Epic)
        val attachmentDtos = listOf(getAttachmentDTO(), getAttachmentDTO())
        val expectedAttachments = persistentListOf(getAttachment(), getAttachment())

        coEvery {
            workItemApi.getAttachments(
                taskPath = "epics",
                objectId = workItemId,
                projectId = projectId
            )
        } returns attachmentDtos
        every { attachmentMapper.toDomain(attachmentDtos) } returns expectedAttachments

        val result = sut.getWorkItemAttachments(workItemId, taskIdentifier)

        assertEquals(expectedAttachments, result)
    }

    @Test
    fun `getWorkItemAttachments for wiki should use wiki path`() = runTest {
        val workItemId = getRandomLong()
        val taskIdentifier = TaskIdentifier.Wiki
        val attachmentDtos = listOf(getAttachmentDTO())
        val expectedAttachments = persistentListOf(getAttachment())

        coEvery {
            workItemApi.getAttachments(
                taskPath = "wiki",
                objectId = workItemId,
                projectId = projectId
            )
        } returns attachmentDtos
        every { attachmentMapper.toDomain(attachmentDtos) } returns expectedAttachments

        val result = sut.getWorkItemAttachments(workItemId, taskIdentifier)

        assertEquals(expectedAttachments, result)
    }

    @Test
    fun `deleteWorkItem should call api delete endpoint`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Task
        val taskPath = WorkItemPathPlural(taskType)

        coJustRun { workItemApi.deleteWorkItem(workItemId = workItemId, taskPath = taskPath) }

        sut.deleteWorkItem(workItemId, taskType)

        coVerify { workItemApi.deleteWorkItem(workItemId = workItemId, taskPath = taskPath) }
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
            createdDate = LocalDateTime.now(),
            modifiedDate = LocalDateTime.now(),
            html = "<p>New wiki content</p>",
            editions = 2L,
            version = version + 1
        )
        val expectedPatchedData = PatchedData(newVersion = version + 1, dueDateStatus = null)

        val jsonSlot = slot<JsonObject>()
        coEvery {
            workItemApi.patchWikiPage(
                pageId = pageId,
                payload = capture(jsonSlot)
            )
        } returns wikiPageDto
        every { patchedDataMapper.fromWiki(wikiPageDto) } returns expectedPatchedData

        val result = sut.patchWikiPage(pageId, version, payload)

        assertEquals(expectedPatchedData, result)
        assertTrue(jsonSlot.captured.containsKey("version"))
        assertTrue(jsonSlot.captured.containsKey("content"))
    }

    @Test
    fun `createWorkItem should create work item and return mapped result`() = runTest {
        val taskType = CommonTaskType.Issue
        val taskPath = WorkItemPathPlural(taskType)
        val subject = getRandomString()
        val description = getRandomString()
        val status = getRandomLong()
        val responseDto = getWorkItemResponseDTO()
        val expectedWorkItem = getWorkItem(taskType = taskType)

        val requestSlot = slot<CreateWorkItemRequestDTO>()
        coEvery {
            workItemApi.createWorkItem(
                taskPath = taskPath,
                createRequest = capture(requestSlot)
            )
        } returns responseDto
        coEvery { workItemMapper.toDomain(responseDto, taskType) } returns expectedWorkItem

        val result = sut.createWorkItem(
            commonTaskType = taskType,
            subject = subject,
            description = description,
            status = status
        )

        assertEquals(expectedWorkItem, result)
        assertEquals(projectId, requestSlot.captured.project)
        assertEquals(subject, requestSlot.captured.subject)
        assertEquals(description, requestSlot.captured.description)
        assertEquals(status, requestSlot.captured.status)
    }

    @Test
    fun `createWorkItem should work without status`() = runTest {
        val taskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathPlural(taskType)
        val subject = getRandomString()
        val description = getRandomString()
        val responseDto = getWorkItemResponseDTO()
        val expectedWorkItem = getWorkItem(taskType = taskType)

        coEvery {
            workItemApi.createWorkItem(
                taskPath = taskPath,
                createRequest = any()
            )
        } returns responseDto
        coEvery { workItemMapper.toDomain(responseDto, taskType) } returns expectedWorkItem

        val result = sut.createWorkItem(
            commonTaskType = taskType,
            subject = subject,
            description = description,
            status = null
        )

        assertEquals(expectedWorkItem, result)
    }

    @Test
    fun `promoteToUserStory should promote issue and return user story`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Issue
        val newUserStoryRef = getRandomLong()
        val userStoryDto = getWorkItemResponseDTO()
        val expectedUserStory = getWorkItem(taskType = CommonTaskType.UserStory)

        coEvery {
            workItemApi.promoteToUserStory(
                taskPath = WorkItemPathPlural(taskType),
                workItemId = workItemId,
                body = PromoteToUserStoryRequestDTO(projectId = projectId)
            )
        } returns listOf(newUserStoryRef)
        coEvery {
            workItemApi.getWorkItemByRef(
                taskPath = WorkItemPathPlural(CommonTaskType.UserStory),
                project = projectId,
                ref = newUserStoryRef
            )
        } returns userStoryDto
        coEvery { workItemMapper.toDomain(userStoryDto, CommonTaskType.UserStory) } returns expectedUserStory

        val result = sut.promoteToUserStory(workItemId, taskType)

        assertEquals(expectedUserStory, result)
    }

    @Test
    fun `promoteToUserStory should promote task and return user story`() = runTest {
        val workItemId = getRandomLong()
        val taskType = CommonTaskType.Task
        val newUserStoryRef = getRandomLong()
        val userStoryDto = getWorkItemResponseDTO()
        val expectedUserStory = getWorkItem(taskType = CommonTaskType.UserStory)

        coEvery {
            workItemApi.promoteToUserStory(
                taskPath = WorkItemPathPlural(taskType),
                workItemId = workItemId,
                body = PromoteToUserStoryRequestDTO(projectId = projectId)
            )
        } returns listOf(newUserStoryRef)
        coEvery {
            workItemApi.getWorkItemByRef(
                taskPath = WorkItemPathPlural(CommonTaskType.UserStory),
                project = projectId,
                ref = newUserStoryRef
            )
        } returns userStoryDto
        coEvery { workItemMapper.toDomain(userStoryDto, CommonTaskType.UserStory) } returns expectedUserStory

        val result = sut.promoteToUserStory(workItemId, taskType)

        assertEquals(expectedUserStory, result)
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

        coEvery {
            workItemApi.promoteToUserStory(
                taskPath = WorkItemPathPlural(taskType),
                workItemId = workItemId,
                body = PromoteToUserStoryRequestDTO(projectId = projectId)
            )
        } returns emptyList()

        assertFailsWith<IllegalStateException> {
            sut.promoteToUserStory(workItemId, taskType)
        }
    }

    @Test
    fun `getWorkItems should work for all task types`() = runTest {
        CommonTaskType.entries.forEach { taskType ->
            val taskPath = WorkItemPathPlural(taskType)
            val dtos = listOf(getWorkItemResponseDTO())
            val expectedItems = persistentListOf(getWorkItem(taskType = taskType))

            coEvery {
                workItemApi.getWorkItems(
                    taskPath = taskPath,
                    project = projectId,
                    assignedId = null,
                    isClosed = null,
                    watcherId = null,
                    isDashboard = null,
                    isBlocked = null,
                    modifiedDateGte = null,
                    finishDateGte = null,
                    sprint = null,
                    pageSize = null
                )
            } returns dtos
            coEvery { workItemMapper.toDomainList(dtos, taskType) } returns expectedItems

            val result = sut.getWorkItems(commonTaskType = taskType, projectId = projectId)

            assertEquals(expectedItems, result)
        }
    }

    @Test
    fun `addAttachment for wiki should use wiki path`() = runTest {
        val workItemId = getRandomLong()
        val fileName = "document.pdf"
        val fileBytes = "pdf content".toByteArray()
        val taskIdentifier = TaskIdentifier.Wiki
        val attachmentDto = getAttachmentDTO()
        val expectedAttachment = getAttachment()

        coEvery {
            workItemApi.uploadCommonTaskAttachment(
                taskPath = "wiki",
                file = any(),
                project = any(),
                objectId = any()
            )
        } returns attachmentDto
        every { attachmentMapper.toDomain(attachmentDto) } returns expectedAttachment

        val result = sut.addAttachment(
            workItemId = workItemId,
            fileName = fileName,
            fileByteArray = fileBytes,
            projectId = projectId,
            taskIdentifier = taskIdentifier
        )

        assertEquals(expectedAttachment, result)
        coVerify {
            workItemApi.uploadCommonTaskAttachment(
                taskPath = "wiki",
                file = any<MultipartBody.Part>(),
                project = any<MultipartBody.Part>(),
                objectId = any<MultipartBody.Part>()
            )
        }
    }

    @Test
    fun `deleteAttachment for wiki should use wiki path`() = runTest {
        val attachment = getAttachment()
        val taskIdentifier = TaskIdentifier.Wiki

        coJustRun {
            workItemApi.deleteAttachment(
                taskPath = "wiki",
                attachmentId = attachment.id
            )
        }

        sut.deleteAttachment(attachment, taskIdentifier)

        coVerify {
            workItemApi.deleteAttachment(
                taskPath = "wiki",
                attachmentId = attachment.id
            )
        }
    }
}
