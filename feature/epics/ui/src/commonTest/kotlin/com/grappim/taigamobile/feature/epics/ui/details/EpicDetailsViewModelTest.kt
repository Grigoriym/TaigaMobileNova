package com.grappim.taigamobile.feature.epics.ui.details

import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.EpicColorUpdateData
import com.grappim.taigamobile.feature.epics.domain.EpicDetailsData
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
import com.grappim.taigamobile.feature.workitem.ui.mappers.WorkItemUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.common_error_message
import com.grappim.taigamobile.strings.generated.resources.epic_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getComment
import com.grappim.taigamobile.testing.models.getEpic
import com.grappim.taigamobile.testing.models.getEpicDetailsData
import com.grappim.taigamobile.testing.models.getStatusUI
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.DeleteAttachmentCall
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeEpicDetailsDataUseCase
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.formatter.decimal.createDecimalFormatter
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.toHex
import kotlinx.collections.immutable.persistentListOf
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

internal class EpicDetailsViewModelTest {
    private val epicId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.Epic)

    private val route = EpicDetailsNavDestination(epicId = epicId, ref = ref)

    private val mainDispatcherRule = MainDispatcherRule()

    private val statusUIMapper = StatusUIMapper()
    private val tagUIMapper = TagUIMapper()
    private val dateTimeUtils: DateTimeUtils = FakeDateTimeUtils()
    private val epicDetailsDataUseCase = FakeEpicDetailsDataUseCase()
    private val workItemRepository = FakeWorkItemRepository()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val historyRepository = FakeHistoryRepository()
    private val usersRepository = FakeUsersRepository()
    private val taigaSessionStorage: TaigaSessionStorage = FakeTaigaSessionStorage(currentUserId = getRandomLong())
    private val workItemsGenerator = WorkItemsGenerator(
        dispatcher = UnconfinedTestDispatcher(),
        statusUIMapper = statusUIMapper
    )
    private val customFieldsUIMapper = CustomFieldsUIMapper(dfSimple = createDecimalFormatter())
    private val workItemUIMapper = WorkItemUIMapper(
        statusUIMapper = statusUIMapper,
        tagUIMapper = tagUIMapper,
        dateTimeUtils = dateTimeUtils
    )
    private val workItemEditStateRepository = WorkItemEditStateRepository()

    private lateinit var sut: EpicDetailsViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = EpicDetailsViewModel(
            route = route,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator,
            historyRepository = historyRepository,
            usersRepository = usersRepository,
            taigaSessionStorage = taigaSessionStorage,
            dateTimeUtils = dateTimeUtils,
            epicDetailsDataUseCase = epicDetailsDataUseCase,
            statusUIMapper = statusUIMapper,
            workItemsGenerator = workItemsGenerator,
            tagUIMapper = tagUIMapper,
            customFieldsUIMapper = customFieldsUIMapper,
            workItemUIMapper = workItemUIMapper,
            workItemEditStateRepository = workItemEditStateRepository
        )
    }

    private fun setupSuccessfulLoad(data: EpicDetailsData = getEpicDetailsData(getEpic(id = epicId))) {
        epicDetailsDataUseCase.getEpicDataResult = Result.success(data)
    }

    private fun setupFailedLoad() {
        epicDetailsDataUseCase.getEpicDataResult = Result.failure(testException)
    }

    @Test
    fun `initial state should have correct toolbar title`() {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        val toolbarTitle = state.toolbarTitle as NativeText.Arguments
        assertEquals(RString.epic_slug, toolbarTitle.stringResource)
        assertEquals(listOf(ref), toolbarTitle.args)
    }

    @Test
    fun `loadEpic success should update state correctly`() {
        val epic = getEpic(id = epicId)
        val epicDetailsData = getEpicDetailsData(epic = epic)
        epicDetailsDataUseCase.getEpicDataResult = Result.success(epicDetailsData)

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.initialLoadError)
        assertNotNull(state.currentEpic)
        assertNotNull(state.originalEpic)
        assertEquals(epicDetailsData.creator, state.creator)
        assertEquals(epicDetailsData.filtersData, state.filtersData)
        assertEquals(epicDetailsData.canDeleteEpic, state.canDeleteEpic)
        assertEquals(epicDetailsData.canModifyEpic, state.canModifyEpic)
        assertEquals(epicDetailsData.canComment, state.canComment)
    }

    @Test
    fun `loadEpic failure should update state with error`() {
        epicDetailsDataUseCase.getEpicDataResult = Result.failure(testException)

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
    fun `retryLoadEpic should reload data`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.retryLoadEpic()

        assertEquals(2, epicDetailsDataUseCase.getEpicDataCallCount)
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

        createViewModel()

        sut.deleteTrigger.test {
            sut.state.value.onDelete()

            assertTrue(awaitItem())
        }

        assertTrue(workItemRepository.deleteWorkItemCalled)
    }

    @Test
    fun `onDelete failure should update state and not emit trigger`() {
        setupSuccessfulLoad()
        workItemRepository.deleteWorkItemThrows = testException

        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        assertTrue(workItemRepository.deleteWorkItemCalled)
    }

    @Test
    fun `setAreWorkItemsExpanded should update state`() {
        setupSuccessfulLoad()
        createViewModel()

        assertFalse(sut.state.value.areWorkItemsExpanded)

        sut.state.value.setAreWorkItemsExpanded(true)

        assertTrue(sut.state.value.areWorkItemsExpanded)

        sut.state.value.setAreWorkItemsExpanded(false)

        assertFalse(sut.state.value.areWorkItemsExpanded)
    }

    @Test
    fun `onGoingToEditTags should set tags in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditTags()

        assertTrue(workItemEditStateRepository.getCurrentTags(epicId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditWatchers should set current watchers in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditWatchers()

        assertTrue(workItemEditStateRepository.getCurrentWatchers(epicId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditAssignee should set current assignee in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.onGoingToEditAssignee()

        assertNotNull(workItemEditStateRepository.getCurrentAssignee(epicId, type))
    }

    @Test
    fun `onGoingToEditAssignee without an assignee should set a null assignee`() {
        setupSuccessfulLoad(
            getEpicDetailsData(epic = getEpic(id = epicId)).copy(assignees = persistentListOf())
        )
        createViewModel()

        sut.onGoingToEditAssignee()

        assertNull(workItemEditStateRepository.getCurrentAssignee(epicId, type))
    }

    @Test
    fun `loadEpic with a null status should still populate the state`() {
        setupSuccessfulLoad(
            getEpicDetailsData(epic = getEpic(id = epicId).copy(status = null))
        )

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.initialLoadError)
        assertNotNull(state.currentEpic)
    }

    @Test
    fun `onDelete without a loaded epic should set the error and not delete`() {
        setupFailedLoad()
        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        assertEquals(NativeText.Resource(RString.common_error_message), sut.state.value.error)
        assertFalse(workItemRepository.deleteWorkItemCalled)
    }

    @Test
    fun `onAttachmentAdd with a null file should set the error and not call the repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onAttachmentAdd(null)

        assertEquals(NativeText.Resource(RString.common_error_message), sut.state.value.error)
        assertTrue(workItemRepository.addAttachmentCalls.isEmpty())
    }

    @Test
    fun `assignees update should be ignored`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        workItemEditStateRepository.updateAssignees(
            epicId,
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

        workItemEditStateRepository.updateAssignee(epicId, type, getRandomLong())

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(newVersion, sut.state.value.currentEpic?.version)
    }

    @Test
    fun `single assignee update failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateAssignee(epicId, type, getRandomLong())

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentEpic?.version)
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

        workItemEditStateRepository.updateWatchers(epicId, type, persistentListOf(getRandomLong()))

        assertEquals(newVersion, sut.state.value.currentEpic?.version)
        assertTrue(sut.watchersState.value.isWatchedByMe)
    }

    @Test
    fun `watchers update failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.updateWatchersDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateWatchers(epicId, type, persistentListOf(getRandomLong()))

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentEpic?.version)
    }

    @Test
    fun `onEpicColorPick success should update the color, the version and the user stories`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        epicDetailsDataUseCase.changeEpicColorResult = Result.success(
            EpicColorUpdateData(
                patchedData = PatchedData(newVersion = newVersion, dueDateStatus = null),
                userStories = persistentListOf(getWorkItem())
            )
        )
        createViewModel()

        sut.state.value.onEpicColorPick(Color.Red)

        val state = sut.state.value
        assertFalse(state.isEpicColorLoading)
        assertEquals(Color.Red.toHex(), state.currentEpic?.epicColor)
        assertEquals(newVersion, state.currentEpic?.version)
        assertEquals(state.currentEpic, state.originalEpic)
        assertEquals(1, state.userStories.size)
    }

    @Test
    fun `onEpicColorPick failure should show a snackbar and stop loading`() = runTest {
        setupSuccessfulLoad()
        epicDetailsDataUseCase.changeEpicColorResult = Result.failure(testException)
        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onEpicColorPick(Color.Red)

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertFalse(sut.state.value.isEpicColorLoading)
    }

    private fun patchedData(newVersion: Long) = PatchedData(newVersion = newVersion, dueDateStatus = null)

    @Test
    fun `onTitleSave success should patch the title and bump the version`() {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version

        sut.titleState.value.onTitleChange(getRandomString())
        sut.state.value.onTitleSave()

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(originalVersion, workItemRepository.patchDataCalls.first().version)
        assertEquals(epicId, workItemRepository.patchDataCalls.first().workItemId)
        assertEquals(newVersion, sut.state.value.currentEpic?.version)
    }

    @Test
    fun `onTitleSave failure should not bump the version`() {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version

        sut.titleState.value.onTitleChange(getRandomString())
        sut.state.value.onTitleSave()

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(originalVersion, sut.state.value.currentEpic?.version)
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

        assertEquals(newVersion, sut.state.value.currentEpic?.version)
        assertEquals(newComments, sut.commentsState.value.comments)
    }

    @Test
    fun `onCreateCommentClick failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version

        sut.snackBarMessage.test {
            sut.state.value.onCreateCommentClick(getRandomString())

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentEpic?.version)
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

        assertEquals(blockNote, sut.state.value.currentEpic?.blockedNote)
        assertEquals(blockNote, sut.state.value.originalEpic?.blockedNote)
        assertEquals(newVersion, sut.state.value.currentEpic?.version)
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
        assertEquals(epicId, call.workItemId)
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

        assertEquals(newVersion, sut.state.value.currentEpic?.version)
    }

    /**
     * [EpicDetailsViewModel.removeAssignee] wires straight into
     * [com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single.WorkItemSingleAssigneeDelegate.handleUnassign] —
     * unlike the multiple-assignee delegate, it does not read a primed "assignee to remove" field,
     * so no state priming is needed here.
     */
    @Test
    fun `removeAssignee failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version

        sut.snackBarMessage.test {
            sut.state.value.removeAssignee()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentEpic?.version)
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

        assertEquals(newVersion, sut.state.value.currentEpic?.version)
        assertTrue(sut.singleAssigneeState.value.isAssignedToMe)
    }

    @Test
    fun `onAssignToMe failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version

        sut.snackBarMessage.test {
            sut.state.value.onAssignToMe()

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentEpic?.version)
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

        assertEquals(newVersion, sut.state.value.currentEpic?.version)
        assertFalse(sut.watchersState.value.watchers.contains(watcher))
    }

    /**
     * Unlike the other handlers in this file, [EpicDetailsViewModel]'s `removeWatcher`'s `doOnError`
     * writes directly into `_state.error` rather than going through [emitError]/`snackBarMessage`.
     */
    @Test
    fun `removeWatcher failure should update the error state and not bump the version`() {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version

        sut.watchersState.value.onRemoveWatcherClick(
            sut.watchersState.value.watchers.first().actualId
        )
        sut.state.value.removeWatcher()

        assertTrue(sut.state.value.error !is NativeText.Empty)
        assertEquals(originalVersion, sut.state.value.currentEpic?.version)
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
        assertEquals(newVersion, sut.state.value.currentEpic?.version)
    }

    @Test
    fun `onBadgeSave failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version
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

        assertEquals(originalVersion, sut.state.value.currentEpic?.version)
    }

    @Test
    fun `description update success should store the description and bump the version`() = runTest {
        val newVersion = getRandomLong()
        val newDescription = getRandomString()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = patchedData(newVersion)
        createViewModel()

        workItemEditStateRepository.updateDescription(epicId, type, newDescription)

        assertEquals(newDescription, sut.state.value.currentEpic?.description)
        assertEquals(newDescription, sut.state.value.originalEpic?.description)
        assertEquals(newVersion, sut.state.value.currentEpic?.version)
    }

    @Test
    fun `description update failure should show a snackbar and keep the description`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalDescription = requireNotNull(sut.state.value.currentEpic).description

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateDescription(epicId, type, getRandomString())

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalDescription, sut.state.value.currentEpic?.description)
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

        workItemEditStateRepository.updateTags(epicId, type, newTags)

        assertEquals(newTags, sut.tagsState.value.tags)
        assertEquals(newVersion, sut.state.value.currentEpic?.version)
    }

    @Test
    fun `tags update failure should show a snackbar and not bump the version`() = runTest {
        setupSuccessfulLoad()
        workItemRepository.patchDataThrows = testException
        createViewModel()
        val originalVersion = requireNotNull(sut.state.value.currentEpic).version

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateTags(
                epicId,
                type,
                persistentListOf(sut.tagsState.value.tags.first().copy(name = getRandomString()))
            )

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertEquals(originalVersion, sut.state.value.currentEpic?.version)
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
        assertEquals(newVersion, sut.state.value.currentEpic?.version)
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
