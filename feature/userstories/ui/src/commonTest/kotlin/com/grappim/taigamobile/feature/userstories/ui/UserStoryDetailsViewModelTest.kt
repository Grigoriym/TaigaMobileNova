@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.userstories.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedCustomAttributes
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.UpdateWorkItem
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.common_error_message
import com.grappim.taigamobile.strings.generated.resources.userstory_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getAttachment
import com.grappim.taigamobile.testing.models.getComment
import com.grappim.taigamobile.testing.models.getStatusUI
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getUserStory
import com.grappim.taigamobile.testing.models.getUserStoryDetailsData
import com.grappim.taigamobile.testing.repo.DeleteAttachmentCall
import com.grappim.taigamobile.testing.repo.EpicLinkCall
import com.grappim.taigamobile.testing.repo.FakeEpicsRepository
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeUserStoryDetailsDataUseCase
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.createTestPlatformFile
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.formatter.decimal.createDecimalFormatter
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class UserStoryDetailsViewModelTest {
    private val userStoryId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.UserStory)

    private val savedStateHandle = SavedStateHandle(
        mapOf("userStoryId" to userStoryId, "ref" to ref)
    )

    private val mainDispatcherRule = MainDispatcherRule()

    private val statusUIMapper = StatusUIMapper()
    private val tagUIMapper = TagUIMapper()
    private val workItemsGenerator = WorkItemsGenerator(
        dispatcher = UnconfinedTestDispatcher(),
        statusUIMapper = statusUIMapper
    )
    private val dateTimeUtils: DateTimeUtils = FakeDateTimeUtils()
    private val userStoryDetailsDataUseCase = FakeUserStoryDetailsDataUseCase()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val historyRepository = FakeHistoryRepository()
    private val workItemRepository = FakeWorkItemRepository()
    private val taigaSessionStorage = FakeTaigaSessionStorage(currentUserId = getRandomLong())
    private val usersRepository = FakeUsersRepository()
    private val epicsRepository = FakeEpicsRepository()
    private val workItemEditStateRepository = WorkItemEditStateRepository()
    private val customFieldsUIMapper = CustomFieldsUIMapper(dfSimple = createDecimalFormatter())

    private lateinit var sut: UserStoryDetailsViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = UserStoryDetailsViewModel(
            savedStateHandle = savedStateHandle,
            userStoryDetailsDataUseCase = userStoryDetailsDataUseCase,
            workItemsGenerator = workItemsGenerator,
            workItemEditStateRepository = workItemEditStateRepository,
            patchDataGenerator = patchDataGenerator,
            statusUIMapper = statusUIMapper,
            tagUIMapper = tagUIMapper,
            dateTimeUtils = dateTimeUtils,
            customFieldsUIMapper = customFieldsUIMapper,
            historyRepository = historyRepository,
            workItemRepository = workItemRepository,
            taigaSessionStorage = taigaSessionStorage,
            usersRepository = usersRepository,
            epicsRepository = epicsRepository
        )
    }

    private fun setupSuccessfulLoad(userStory: UserStory = getUserStory(id = userStoryId)) {
        userStoryDetailsDataUseCase.getUserStoryDataResult = Result.success(
            getUserStoryDetailsData(userStory = userStory)
        )
        userStoryDetailsDataUseCase.getUserStoryResult = userStory
    }

    private fun setupFailedLoad() {
        userStoryDetailsDataUseCase.getUserStoryDataResult = Result.failure(testException)
    }

    private fun patchedData(newVersion: Long) = PatchedData(newVersion = newVersion, dueDateStatus = null)

    private fun userStoryWithEpics(vararg epicIds: Long): UserStory = getUserStory(id = userStoryId)
        .copy(
            userStoryEpics = epicIds.map { epicId ->
                UserStoryEpic(
                    id = epicId,
                    title = getRandomString(),
                    ref = getRandomLong(),
                    color = getRandomString()
                )
            }.toPersistentList()
        )

    @Test
    fun `initial state should have correct toolbar title`() {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        val toolbarTitle = state.toolbarTitle as NativeText.Arguments
        assertEquals(RString.userstory_slug, toolbarTitle.stringResource)
        assertEquals(listOf(ref), toolbarTitle.args)
    }

    @Test
    fun `loadUserStory success should update state correctly`() {
        val userStory = getUserStory(id = userStoryId)
        val userStoryDetailsData = getUserStoryDetailsData(userStory = userStory)
        userStoryDetailsDataUseCase.getUserStoryDataResult = Result.success(userStoryDetailsData)

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.initialLoadError)
        assertNotNull(state.currentUserStory)
        assertNotNull(state.originalUserStory)
        assertEquals(userStoryDetailsData.sprint, state.sprint)
        assertEquals(userStoryDetailsData.creator, state.creator)
        assertEquals(userStoryDetailsData.filtersData, state.filtersData)
        assertTrue(state.canEditUserStory)
        assertTrue(state.canDeleteUserStory)
        assertTrue(state.canComment)
        assertTrue(state.canModifyRelatedEpic)
    }

    @Test
    fun `loadUserStory failure should update state with error`() {
        userStoryDetailsDataUseCase.getUserStoryDataResult = Result.failure(testException)

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.initialLoadError !is NativeText.Empty)
    }

    @Test
    fun `setDropdownMenuExpanded should update state`() {
        setupSuccessfulLoad()
        createViewModel()

        assertFalse(sut.state.value.isDropdownMenuExpanded)

        sut.state.value.setDropdownMenuExpanded(true)

        assertTrue(sut.state.value.isDropdownMenuExpanded)

        sut.state.value.setDropdownMenuExpanded(false)

        assertFalse(sut.state.value.isDropdownMenuExpanded)
    }

    @Test
    fun `retryLoadUserStory should reload data`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.retryLoadUserStory()

        assertEquals(2, userStoryDetailsDataUseCase.getUserStoryDataCallCount)
    }

    @Test
    fun `setIsDeleteDialogVisible should update state`() {
        setupSuccessfulLoad()
        createViewModel()

        assertFalse(sut.state.value.isDeleteDialogVisible)

        sut.state.value.setIsDeleteDialogVisible(true)

        assertTrue(sut.state.value.isDeleteDialogVisible)

        sut.state.value.setIsDeleteDialogVisible(false)

        assertFalse(sut.state.value.isDeleteDialogVisible)
    }

    @Test
    fun `onDelete success should emit delete trigger`() = runTest {
        setupSuccessfulLoad()
        userStoryDetailsDataUseCase.deleteUserStoryResult = Result.success(Unit)

        createViewModel()

        sut.deleteTrigger.test {
            sut.state.value.onDelete()

            assertTrue(awaitItem())
        }

        assertEquals(1, userStoryDetailsDataUseCase.deleteUserStoryCallCount)
    }

    @Test
    fun `onDelete failure should update state and not emit trigger`() {
        setupSuccessfulLoad()
        userStoryDetailsDataUseCase.deleteUserStoryResult = Result.failure(testException)

        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        assertEquals(1, userStoryDetailsDataUseCase.deleteUserStoryCallCount)
    }

    @Test
    fun `onEditTags should set tags in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onEditTags()

        assertTrue(workItemEditStateRepository.getCurrentTags(userStoryId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditWatchers should set current watchers in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditWatchers()

        assertTrue(workItemEditStateRepository.getCurrentWatchers(userStoryId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditAssignees should set current assignees in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditAssignees()

        assertTrue(workItemEditStateRepository.getCurrentAssignees(userStoryId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditEpics should set current epics in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditEpics()

        assertTrue(workItemEditStateRepository.getCurrentEpics(userStoryId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditEpics without a loaded user story should set no epics`() {
        setupFailedLoad()
        createViewModel()

        sut.state.value.onGoingToEditEpics()

        assertTrue(workItemEditStateRepository.getCurrentEpics(userStoryId, type).isEmpty())
    }

    @Test
    fun `loadUserStory with a null status should still populate the state`() {
        setupSuccessfulLoad(getUserStory(id = userStoryId).copy(status = null))

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.initialLoadError)
        assertNotNull(state.currentUserStory)
    }

    @Test
    fun `onAttachmentAdd with a null file should set the error and not call the repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onAttachmentAdd(null)

        assertEquals(NativeText.Resource(RString.common_error_message), sut.state.value.error)
        assertTrue(workItemRepository.addAttachmentCalls.isEmpty())
    }

    /**
     * [io.github.vinceglb.filekit.PlatformFile.readBytes] runs on a real IO dispatcher, so the
     * `viewModelScope.launch` does not complete before the call returns the way every other
     * handler here does. The state has to be awaited rather than read.
     */
    @Test
    fun `onAttachmentAdd with a file should add the attachment`() = runTest {
        setupSuccessfulLoad()
        val attachment = getAttachment()
        workItemRepository.addAttachmentResult = attachment
        createViewModel()

        sut.attachmentsState.test {
            val initial = awaitItem()

            sut.state.value.onAttachmentAdd(
                createTestPlatformFile(getRandomString(), byteArrayOf(1, 2, 3))
            )

            assertTrue(awaitItem().areAttachmentsLoading)

            val loaded = awaitItem()
            assertFalse(loaded.areAttachmentsLoading)
            assertEquals(initial.attachments + attachment, loaded.attachments)
        }

        assertEquals(1, workItemRepository.addAttachmentCalls.size)
        assertEquals(userStoryId, workItemRepository.addAttachmentCalls.first().workItemId)
    }

    @Test
    fun `onDelete without a loaded user story should set the error and not delete`() {
        setupFailedLoad()
        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        assertEquals(NativeText.Resource(RString.common_error_message), sut.state.value.error)
        assertEquals(0, userStoryDetailsDataUseCase.deleteUserStoryCallCount)
    }

    @Test
    fun `single assignee update should be ignored`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        workItemEditStateRepository.updateAssignee(userStoryId, type, getRandomLong())

        assertTrue(workItemRepository.patchDataCalls.isEmpty())
    }

    @Test
    fun `assignees update should patch the assignees and bump the version`() = runTest {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = PatchedData(
            newVersion = newVersion,
            dueDateStatus = null
        )
        usersRepository.getUsersListResult = persistentListOf(getUser())
        createViewModel()

        workItemEditStateRepository.updateAssignees(
            userStoryId,
            type,
            persistentListOf(getRandomLong())
        )

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `watchers update should update the watchers and bump the version`() = runTest {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.updateWatchersDataResult = WatchersListUpdateData(
            version = newVersion,
            isWatchedByMe = true,
            watchers = persistentListOf(getUser())
        )
        createViewModel()

        workItemEditStateRepository.updateWatchers(
            userStoryId,
            type,
            persistentListOf(getRandomLong())
        )

        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
        assertTrue(sut.watchersState.value.isWatchedByMe)
    }

    @Test
    fun `onEpicRemoveClick success should unlink the epic and reload the user story`() {
        val epicId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(epicId))
        val reloaded = userStoryWithEpics()
        userStoryDetailsDataUseCase.getUserStoryResult = reloaded
        createViewModel()

        sut.state.value.onEpicRemoveClick(epicId)

        assertEquals(
            listOf(EpicLinkCall(epicId = epicId, userStoryId = userStoryId)),
            epicsRepository.unlinkFromEpicCalls
        )
        assertEquals(reloaded, sut.state.value.currentUserStory)
        assertEquals(reloaded, sut.state.value.originalUserStory)
        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicRemoveClick failure should show a snackbar and stop loading`() = runTest {
        val epicId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(epicId))
        epicsRepository.unlinkFromEpicThrows = testException
        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onEpicRemoveClick(epicId)

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicsUpdate with unchanged ids should do nothing`() = runTest {
        val epicId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(epicId))
        createViewModel()

        workItemEditStateRepository.updateEpics(userStoryId, type, persistentListOf(epicId))

        assertTrue(epicsRepository.linkToEpicCalls.isEmpty())
        assertTrue(epicsRepository.unlinkFromEpicCalls.isEmpty())
        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicsUpdate with an added id should link the epic and reload`() = runTest {
        val keptId = getRandomLong()
        val addedId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(keptId))
        val reloaded = userStoryWithEpics(keptId, addedId)
        userStoryDetailsDataUseCase.getUserStoryResult = reloaded
        createViewModel()

        workItemEditStateRepository.updateEpics(
            userStoryId,
            type,
            persistentListOf(keptId, addedId)
        )

        assertEquals(
            listOf(EpicLinkCall(epicId = addedId, userStoryId = userStoryId)),
            epicsRepository.linkToEpicCalls
        )
        assertTrue(epicsRepository.unlinkFromEpicCalls.isEmpty())
        assertEquals(reloaded, sut.state.value.currentUserStory)
        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicsUpdate with only a removed id should unlink the epic and reload`() = runTest {
        val removedId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(removedId))
        val reloaded = userStoryWithEpics()
        userStoryDetailsDataUseCase.getUserStoryResult = reloaded
        createViewModel()

        workItemEditStateRepository.updateEpics(userStoryId, type, persistentListOf())

        assertEquals(
            listOf(EpicLinkCall(epicId = removedId, userStoryId = userStoryId)),
            epicsRepository.unlinkFromEpicCalls
        )
        assertTrue(epicsRepository.linkToEpicCalls.isEmpty())
        assertEquals(reloaded, sut.state.value.currentUserStory)
        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicsUpdate failure should show a snackbar and stop loading`() = runTest {
        setupSuccessfulLoad(userStoryWithEpics())
        epicsRepository.linkToEpicThrows = testException
        createViewModel()

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateEpics(
                userStoryId,
                type,
                persistentListOf(getRandomLong())
            )

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onTitleSave success should patch the title and bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.titleState.value.onTitleChange(getRandomString())
        sut.state.value.onTitleSave()

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(originalVersion, workItemRepository.patchDataCalls.first().version)
        assertEquals(userStoryId, workItemRepository.patchDataCalls.first().workItemId)
        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onTitleSave failure should not bump the version`() {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.titleState.value.onTitleChange(getRandomString())
        sut.state.value.onTitleSave()

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onBadgeSave success should patch the badge and bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()
        val status = getStatusUI()

        sut.state.value.onBadgeSave(
            SelectableWorkItemBadgeStatus(
                options = persistentListOf(status),
                currentValue = status
            ),
            status
        )

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onBadgeSave failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version
        val status = getStatusUI()

        sut.snackBarMessage.test {
            sut.state.value.onBadgeSave(
                SelectableWorkItemBadgeStatus(
                    options = persistentListOf(status),
                    currentValue = status
                ),
                status
            )

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onCreateCommentClick success should reload the comments and bump the version`() {
        val newVersion = getRandomLong()
        val newComments = persistentListOf(getComment())
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        historyRepository.getCommentsResult = newComments
        createViewModel()

        sut.state.value.onCreateCommentClick(getRandomString())

        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
        assertEquals(newComments, sut.commentsState.value.comments)
    }

    @Test
    fun `onCreateCommentClick failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.snackBarMessage.test {
            sut.state.value.onCreateCommentClick(getRandomString())

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onCommentRemove success should drop the comment from the state`() {
        setupSuccessfulLoad()
        createViewModel()
        val comment = sut.commentsState.value.comments.first()

        sut.state.value.onCommentRemove(comment)

        assertFalse(sut.commentsState.value.comments.contains(comment))
    }

    @Test
    fun `onCommentRemove failure should show a snackbar and keep the comment`() = runTest {
        setupSuccessfulLoad()
        historyRepository.deleteCommentThrows = testException
        createViewModel()
        val comment = sut.commentsState.value.comments.first()

        sut.snackBarMessage.test {
            sut.state.value.onCommentRemove(comment)

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertTrue(sut.commentsState.value.comments.contains(comment))
    }

    @Test
    fun `onAttachmentRemove success should drop the attachment from the state`() {
        setupSuccessfulLoad()
        createViewModel()
        val attachment = sut.attachmentsState.value.attachments.first()

        sut.state.value.onAttachmentRemove(attachment)

        assertEquals(
            listOf(DeleteAttachmentCall(attachment = attachment, taskIdentifier = type)),
            workItemRepository.deleteAttachmentCalls
        )
        assertFalse(sut.attachmentsState.value.attachments.contains(attachment))
    }

    @Test
    fun `onAttachmentRemove failure should show a snackbar and keep the attachment`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.deleteAttachmentThrows = testException
        createViewModel()
        val attachment = sut.attachmentsState.value.attachments.first()

        sut.snackBarMessage.test {
            sut.state.value.onAttachmentRemove(attachment)

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertTrue(sut.attachmentsState.value.attachments.contains(attachment))
    }

    @Test
    fun `onBlockToggle success should store the block note and bump the version`() {
        val newVersion = getRandomLong()
        val blockNote = getRandomString()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()

        sut.state.value.onBlockToggle(true, blockNote)

        assertEquals(blockNote, sut.state.value.currentUserStory?.blockedNote)
        assertEquals(blockNote, sut.state.value.originalUserStory?.blockedNote)
        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `onBlockToggle failure should show a snackbar and stop loading`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onBlockToggle(true, getRandomString())

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `onCustomFieldSave success should patch with the loaded custom fields version`() {
        setupSuccessfulLoad()
        workItemRepository.patchCustomAttributesResult =
            PatchedCustomAttributes(version = getRandomLong())
        createViewModel()
        val item = sut.customFieldsState.value.customFieldStateItems.first()

        sut.state.value.onCustomFieldSave(item)

        assertEquals(1, workItemRepository.patchCustomAttributesCalls.size)
        val call = workItemRepository.patchCustomAttributesCalls.first()
        assertEquals(sut.state.value.customFieldsVersion, call.customAttributesVersion)
        assertEquals(userStoryId, call.workItemId)
    }

    @Test
    fun `onCustomFieldSave failure should show a snackbar`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchCustomAttributesThrows = testException
        createViewModel()
        val item = sut.customFieldsState.value.customFieldStateItems.first()

        sut.snackBarMessage.test {
            sut.state.value.onCustomFieldSave(item)

            assertTrue(awaitItem() !is NativeText.Empty)
        }
    }

    @Test
    fun `removeAssignee success should bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        usersRepository.getUsersListResult = persistentListOf(getUser())
        createViewModel()

        sut.multipleAssigneesState.value.onRemoveAssigneeClick(
            sut.multipleAssigneesState.value.assignees.first()
        )
        sut.state.value.removeAssignee()

        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `removeAssignee failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.snackBarMessage.test {
            sut.multipleAssigneesState.value.onRemoveAssigneeClick(
                sut.multipleAssigneesState.value.assignees.first()
            )
            sut.state.value.removeAssignee()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onAssignToMe success should bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        usersRepository.getUsersListResult = persistentListOf(getUser())
        usersRepository.isAnyAssignedToMeResult = true
        createViewModel()

        sut.state.value.onAssignToMe()

        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
        assertTrue(sut.multipleAssigneesState.value.isAssignedToMe)
    }

    @Test
    fun `onAssignToMe failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.snackBarMessage.test {
            sut.state.value.onAssignToMe()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `removeWatcher success should bump the version and drop the watcher`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()
        val watcher = sut.watchersState.value.watchers.first()

        sut.watchersState.value.onRemoveWatcherClick(watcher.actualId)
        sut.state.value.removeWatcher()

        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
        assertFalse(sut.watchersState.value.watchers.contains(watcher))
    }

    @Test
    fun `removeWatcher failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.snackBarMessage.test {
            sut.watchersState.value.onRemoveWatcherClick(
                sut.watchersState.value.watchers.first().actualId
            )
            sut.state.value.removeWatcher()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onAddMeToWatchersClick success should refresh the watchers`() {
        val watchers = persistentListOf(getUser())
        setupSuccessfulLoad()
        workItemRepository.getUpdateWorkItemResult = UpdateWorkItem(persistentListOf(getRandomLong()))
        usersRepository.getUsersListResult = watchers
        usersRepository.isAnyAssignedToMeResult = true
        createViewModel()

        sut.state.value.onAddMeToWatchersClick()

        assertTrue(workItemRepository.watchWorkItemCalled)
        assertEquals(watchers, sut.watchersState.value.watchers)
        assertTrue(sut.watchersState.value.isWatchedByMe)
    }

    @Test
    fun `onAddMeToWatchersClick failure should show a snackbar`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.watchWorkItemThrows = testException
        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onAddMeToWatchersClick()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertFalse(sut.watchersState.value.areWatchersLoading)
    }

    @Test
    fun `onRemoveMeFromWatchersClick success should refresh the watchers`() {
        val watchers = persistentListOf(getUser())
        setupSuccessfulLoad()
        workItemRepository.getUpdateWorkItemResult = UpdateWorkItem(persistentListOf(getRandomLong()))
        usersRepository.getUsersListResult = watchers
        usersRepository.isAnyAssignedToMeResult = false
        createViewModel()

        sut.state.value.onRemoveMeFromWatchersClick()

        assertTrue(workItemRepository.unwatchWorkItemCalled)
        assertEquals(watchers, sut.watchersState.value.watchers)
        assertFalse(sut.watchersState.value.isWatchedByMe)
    }

    @Test
    fun `onRemoveMeFromWatchersClick failure should show a snackbar`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.unwatchWorkItemThrows = testException
        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onRemoveMeFromWatchersClick()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertFalse(sut.watchersState.value.areWatchersLoading)
    }

    @Test
    fun `setDueDate success should bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()

        sut.state.value.setDueDate(getRandomLong())

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
        assertEquals(newVersion, sut.state.value.originalUserStory?.version)
    }

    @Test
    fun `setDueDate failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.snackBarMessage.test {
            sut.state.value.setDueDate(null)

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onTagRemove success should drop the tag and bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()
        val tag = sut.tagsState.value.tags.first()

        sut.state.value.onTagRemove(tag)

        assertFalse(sut.tagsState.value.tags.contains(tag))
        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onTagRemove failure should show a snackbar and keep the tag`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val tag = sut.tagsState.value.tags.first()

        sut.snackBarMessage.test {
            sut.state.value.onTagRemove(tag)

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertTrue(sut.tagsState.value.tags.contains(tag))
    }

    @Test
    fun `tags update success should replace the tags and bump the version`() = runTest {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()
        val newTags = persistentListOf(
            sut.tagsState.value.tags.first().copy(name = getRandomString())
        )

        workItemEditStateRepository.updateTags(userStoryId, type, newTags)

        assertEquals(newTags, sut.tagsState.value.tags)
        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `tags update failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateTags(
                userStoryId,
                type,
                persistentListOf(sut.tagsState.value.tags.first().copy(name = getRandomString()))
            )

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `description update success should store the description and bump the version`() = runTest {
        val newVersion = getRandomLong()
        val newDescription = getRandomString()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()

        workItemEditStateRepository.updateDescription(userStoryId, type, newDescription)

        assertEquals(newDescription, sut.state.value.currentUserStory?.description)
        assertEquals(newDescription, sut.state.value.originalUserStory?.description)
        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `onAttachmentAdd failure should show a snackbar`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.addAttachmentThrows = testException
        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onAttachmentAdd(
                createTestPlatformFile(getRandomString(), byteArrayOf(1, 2, 3))
            )

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertFalse(sut.attachmentsState.value.areAttachmentsLoading)
    }

    @Test
    fun `assignees update failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateAssignees(
                userStoryId,
                type,
                persistentListOf(getRandomLong())
            )

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `watchers update failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.updateWatchersDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentUserStory).version

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateWatchers(
                userStoryId,
                type,
                persistentListOf(getRandomLong())
            )

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `description update failure should show a snackbar and keep the description`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalDescription = requireNotNull(sut.state.value.currentUserStory).description

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateDescription(userStoryId, type, getRandomString())

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalDescription, sut.state.value.currentUserStory?.description)
    }
}
