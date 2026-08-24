@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.tasks.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.tasks.domain.TaskDetailsData
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
import com.grappim.taigamobile.strings.generated.resources.task_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getComment
import com.grappim.taigamobile.testing.models.getStatusUI
import com.grappim.taigamobile.testing.models.getTask
import com.grappim.taigamobile.testing.models.getTaskDetailsData
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.DeleteAttachmentCall
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeTaskDetailsDataUseCase
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.formatter.decimal.createDecimalFormatter
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TaskDetailsViewModelTest {
    private val taskId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.Task)

    private val savedStateHandle = SavedStateHandle(
        mapOf("taskId" to taskId, "ref" to ref)
    )

    private val mainDispatcherRule = MainDispatcherRule()

    private val statusUIMapper = StatusUIMapper()
    private val tagUIMapper = TagUIMapper()
    private val workItemsGenerator = WorkItemsGenerator(
        dispatcher = UnconfinedTestDispatcher(),
        statusUIMapper = statusUIMapper
    )
    private val dateTimeUtils = FakeDateTimeUtils()
    private val taskDetailsDataUseCase = FakeTaskDetailsDataUseCase()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val historyRepository = FakeHistoryRepository()
    private val workItemRepository = FakeWorkItemRepository()
    private val taigaSessionStorage: TaigaSessionStorage = FakeTaigaSessionStorage(currentUserId = getRandomLong())
    private val usersRepository = FakeUsersRepository()
    private val workItemEditStateRepository = WorkItemEditStateRepository()
    private val customFieldsUIMapper = CustomFieldsUIMapper(dfSimple = createDecimalFormatter())

    private lateinit var sut: TaskDetailsViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = TaskDetailsViewModel(
            savedStateHandle = savedStateHandle,
            taskDetailsDataUseCase = taskDetailsDataUseCase,
            workItemsGenerator = workItemsGenerator,
            patchDataGenerator = patchDataGenerator,
            statusUIMapper = statusUIMapper,
            tagUIMapper = tagUIMapper,
            dateTimeUtils = dateTimeUtils,
            customFieldsUIMapper = customFieldsUIMapper,
            historyRepository = historyRepository,
            workItemRepository = workItemRepository,
            taigaSessionStorage = taigaSessionStorage,
            usersRepository = usersRepository,
            workItemEditStateRepository = workItemEditStateRepository
        )
    }

    private fun setupSuccessfulLoad(taskDetailsData: TaskDetailsData? = null) {
        val data = taskDetailsData ?: getTaskDetailsData(task = getTask(id = taskId))
        taskDetailsDataUseCase.getTaskDataResult = Result.success(data)
    }

    private fun setupFailedLoad() {
        taskDetailsDataUseCase.getTaskDataResult = Result.failure(testException)
    }

    @Test
    fun `initial state should have correct toolbar title`() {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        val toolbarTitle = state.toolbarTitle as NativeText.Arguments
        assertEquals(RString.task_slug, toolbarTitle.stringResource)
        assertEquals(listOf(ref), toolbarTitle.args)
    }

    @Test
    fun `loadTask success should update state correctly`() {
        val task = getTask(id = taskId)
        val taskDetailsData = getTaskDetailsData(task = task)
        setupSuccessfulLoad(taskDetailsData)

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.initialLoadError)
        assertNotNull(state.currentTask)
        assertNotNull(state.originalTask)
        assertEquals(taskDetailsData.sprint, state.sprint)
        assertEquals(taskDetailsData.creator, state.creator)
        assertEquals(taskDetailsData.filtersData, state.filtersData)
        assertEquals(taskDetailsData.canDeleteTask, state.canDeleteTask)
        assertEquals(taskDetailsData.canModifyTask, state.canModifyTask)
        assertEquals(taskDetailsData.canComment, state.canComment)
    }

    @Test
    fun `loadTask failure should update state with error`() {
        taskDetailsDataUseCase.getTaskDataResult = Result.failure(testException)

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
    fun `retryLoadTask should reload data`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.retryLoadTask()

        assertEquals(2, taskDetailsDataUseCase.getTaskDataCallCount)
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
        taskDetailsDataUseCase.deleteTaskResult = Result.success(Unit)

        createViewModel()

        sut.deleteTrigger.test {
            sut.state.value.onDelete()

            assertTrue(awaitItem())
        }

        assertEquals(1, taskDetailsDataUseCase.deleteTaskCallCount)
    }

    @Test
    fun `onDelete failure should update state and not emit trigger`() {
        setupSuccessfulLoad()
        taskDetailsDataUseCase.deleteTaskResult = Result.failure(testException)

        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        assertEquals(1, taskDetailsDataUseCase.deleteTaskCallCount)
    }

    @Test
    fun `onGoingToEditTags should set tags in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditTags()

        assertTrue(workItemEditStateRepository.getCurrentTags(taskId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditWatchers should set current watchers in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditWatchers()

        assertTrue(workItemEditStateRepository.getCurrentWatchers(taskId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditAssignee should set current assignee in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.onGoingToEditAssignee()

        assertNotNull(workItemEditStateRepository.getCurrentAssignee(taskId, type))
    }

    @Test
    fun `promoteToUserStory success should emit trigger`() = runTest {
        setupSuccessfulLoad()
        val workItem = getWorkItem()
        workItemRepository.promoteToUserStoryResult = workItem

        createViewModel()

        sut.promotedToUserStoryTrigger.test {
            sut.state.value.onPromoteClick()

            assertEquals(workItem, awaitItem())
        }

        assertFalse(sut.state.value.isLoading)
        assertTrue(workItemRepository.promoteToUserStoryCalled)
    }

    @Test
    fun `promoteToUserStory failure should show snackbar error`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.promoteToUserStoryThrows = testException

        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onPromoteClick()

            val message = awaitItem()
            assertTrue(message !is NativeText.Empty)
        }

        assertFalse(sut.state.value.isLoading)
        assertTrue(workItemRepository.promoteToUserStoryCalled)
    }

    @Test
    fun `onGoingToEditAssignee without an assignee should set a null assignee`() {
        setupSuccessfulLoad(
            getTaskDetailsData(task = getTask(id = taskId)).copy(assignees = persistentListOf())
        )
        createViewModel()

        sut.onGoingToEditAssignee()

        assertNull(workItemEditStateRepository.getCurrentAssignee(taskId, type))
    }

    @Test
    fun `loadTask with a null status should still populate the state without a status badge`() {
        setupSuccessfulLoad(
            getTaskDetailsData(task = getTask(id = taskId).copy(status = null))
        )

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.initialLoadError)
        assertNotNull(state.currentTask)
        assertTrue(sut.badgeState.value.workItemBadges.isEmpty())
    }

    @Test
    fun `onDelete without a loaded task should set the error and not delete`() {
        setupFailedLoad()
        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        assertEquals(NativeText.Resource(RString.common_error_message), sut.state.value.error)
        assertEquals(0, taskDetailsDataUseCase.deleteTaskCallCount)
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
     * [TaskDetailsViewModel] mixes in the *single* assignee delegate, so
     * [com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate.Assignees] is the
     * no-op arm of `handleTeamMemberUpdate` and `Assignee` is the one that patches.
     */
    @Test
    fun `assignees update should be ignored`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        workItemEditStateRepository.updateAssignees(
            taskId,
            type,
            persistentListOf(getRandomLong())
        )

        assertTrue(workItemRepository.patchDataCalls.isEmpty())
    }

    @Test
    fun `single assignee update should patch the assignee and bump the version`() = runTest {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = PatchedData(
            newVersion = newVersion,
            dueDateStatus = null
        )
        usersRepository.getUsersListResult = persistentListOf(getUser())
        createViewModel()

        workItemEditStateRepository.updateAssignee(taskId, type, getRandomLong())

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(newVersion, sut.state.value.currentTask?.version)
    }

    @Test
    fun `single assignee update failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateAssignee(taskId, type, getRandomLong())

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
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

        workItemEditStateRepository.updateWatchers(taskId, type, persistentListOf(getRandomLong()))

        assertEquals(newVersion, sut.state.value.currentTask?.version)
        assertTrue(sut.watchersState.value.isWatchedByMe)
    }

    @Test
    fun `watchers update failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.updateWatchersDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateWatchers(taskId, type, persistentListOf(getRandomLong()))

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
    }

    private fun patchedData(newVersion: Long) = PatchedData(newVersion = newVersion, dueDateStatus = null)

    @Test
    fun `onTitleSave success should patch the title and bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.titleState.value.onTitleChange(getRandomString())
        sut.state.value.onTitleSave()

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(originalVersion, workItemRepository.patchDataCalls.first().version)
        assertEquals(taskId, workItemRepository.patchDataCalls.first().workItemId)
        assertEquals(newVersion, sut.state.value.currentTask?.version)
    }

    @Test
    fun `onTitleSave failure should not bump the version`() {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.titleState.value.onTitleChange(getRandomString())
        sut.state.value.onTitleSave()

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(originalVersion, sut.state.value.currentTask?.version)
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

        assertEquals(newVersion, sut.state.value.currentTask?.version)
        assertEquals(newComments, sut.commentsState.value.comments)
    }

    @Test
    fun `onCreateCommentClick failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.snackBarMessage.test {
            sut.state.value.onCreateCommentClick(getRandomString())

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
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

        assertEquals(blockNote, sut.state.value.currentTask?.blockedNote)
        assertEquals(blockNote, sut.state.value.originalTask?.blockedNote)
        assertEquals(newVersion, sut.state.value.currentTask?.version)
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
        assertEquals(taskId, call.workItemId)
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

        sut.state.value.removeAssignee()

        assertEquals(newVersion, sut.state.value.currentTask?.version)
    }

    @Test
    fun `removeAssignee failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.snackBarMessage.test {
            sut.state.value.removeAssignee()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
    }

    /**
     * [TaskDetailsViewModel] wires both `onUnassign` and `removeAssignee` to the same
     * [com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single.WorkItemSingleAssigneeDelegate.handleUnassign]
     * call — unlike the multiple-assignee delegate, it does not read a primed "assignee to remove"
     * field, so no state priming is needed here.
     */
    @Test
    fun `onUnassign success should bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        usersRepository.getUsersListResult = persistentListOf(getUser())
        createViewModel()

        sut.state.value.onUnassign()

        assertEquals(newVersion, sut.state.value.currentTask?.version)
    }

    @Test
    fun `onUnassign failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.snackBarMessage.test {
            sut.state.value.onUnassign()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
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

        assertEquals(newVersion, sut.state.value.currentTask?.version)
        assertTrue(sut.singleAssigneeState.value.isAssignedToMe)
    }

    @Test
    fun `onAssignToMe failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.snackBarMessage.test {
            sut.state.value.onAssignToMe()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
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

        assertEquals(newVersion, sut.state.value.currentTask?.version)
        assertFalse(sut.watchersState.value.watchers.contains(watcher))
    }

    @Test
    fun `removeWatcher failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.snackBarMessage.test {
            sut.watchersState.value.onRemoveWatcherClick(
                sut.watchersState.value.watchers.first().actualId
            )
            sut.state.value.removeWatcher()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
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
        assertEquals(newVersion, sut.state.value.currentTask?.version)
    }

    @Test
    fun `onBadgeSave failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version
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

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
    }

    @Test
    fun `description update success should store the description and bump the version`() = runTest {
        val newVersion = getRandomLong()
        val newDescription = getRandomString()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()

        workItemEditStateRepository.updateDescription(taskId, type, newDescription)

        assertEquals(newDescription, sut.state.value.currentTask?.description)
        assertEquals(newDescription, sut.state.value.originalTask?.description)
        assertEquals(newVersion, sut.state.value.currentTask?.version)
    }

    @Test
    fun `description update failure should show a snackbar and keep the description`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalDescription = requireNotNull(sut.state.value.currentTask).description

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateDescription(taskId, type, getRandomString())

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalDescription, sut.state.value.currentTask?.description)
    }

    @Test
    fun `setDueDate success should bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()

        sut.state.value.setDueDate(getRandomLong())

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(newVersion, sut.state.value.currentTask?.version)
        assertEquals(newVersion, sut.state.value.originalTask?.version)
    }

    @Test
    fun `setDueDate failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.snackBarMessage.test {
            sut.state.value.setDueDate(null)

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
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

        workItemEditStateRepository.updateTags(taskId, type, newTags)

        assertEquals(newTags, sut.tagsState.value.tags)
        assertEquals(newVersion, sut.state.value.currentTask?.version)
    }

    @Test
    fun `tags update failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentTask).version

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateTags(
                taskId,
                type,
                persistentListOf(sut.tagsState.value.tags.first().copy(name = getRandomString()))
            )

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentTask?.version)
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
        assertEquals(newVersion, sut.state.value.currentTask?.version)
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
}
