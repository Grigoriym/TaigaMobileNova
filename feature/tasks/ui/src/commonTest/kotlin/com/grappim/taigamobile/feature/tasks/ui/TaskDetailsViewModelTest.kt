@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.tasks.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.tasks.domain.TaskDetailsData
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.common_error_message
import com.grappim.taigamobile.strings.generated.resources.task_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getAttachment
import com.grappim.taigamobile.testing.models.getTask
import com.grappim.taigamobile.testing.models.getTaskDetailsData
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeTaskDetailsDataUseCase
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.createTestPlatformFile
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
    private val historyRepository: HistoryRepository = FakeHistoryRepository()
    private val workItemRepository = FakeWorkItemRepository()
    private val taigaSessionStorage: TaigaSessionStorage = FakeTaigaSessionStorage()
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
        assertEquals(taskId, workItemRepository.addAttachmentCalls.first().workItemId)
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
}
