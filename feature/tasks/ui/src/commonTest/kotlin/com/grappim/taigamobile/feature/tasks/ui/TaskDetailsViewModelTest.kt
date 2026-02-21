@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.tasks.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.tasks.domain.TaskDetailsDataUseCase
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.task_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getTask
import com.grappim.taigamobile.testing.getTaskDetailsData
import com.grappim.taigamobile.testing.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class FakeDateTimeUtils : DateTimeUtils {
    override fun retrieveEpochMillisAtStartOfDay(localDate: LocalDate): Long = 0L
    override fun fromMillisToLocalDate(millis: Long): LocalDate = LocalDate(2024, 1, 1)
    override fun parseLocalDateToString(localDate: LocalDate): String = localDate.toString()
    override fun formatToMediumFormat(localDate: LocalDate): String = "Jan 1, 2024"
    override fun formatToMediumFormat(localDateTime: LocalDateTime): String = "Jan 1, 2024"
    override fun parseToLocalDate(text: String): LocalDate = LocalDate(2024, 1, 1)
    override fun getLocalDateNow(): LocalDate = LocalDate(2024, 1, 1)
}

class TaskDetailsViewModelTest {
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
    private val dateTimeUtils: DateTimeUtils = FakeDateTimeUtils()

    private val taskDetailsDataUseCase: TaskDetailsDataUseCase = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val fileUriManager: FileUriManager = mockk()
    private val customFieldsUIMapper: CustomFieldsUIMapper = mockk()
    private val historyRepository: HistoryRepository = mockk()
    private val workItemRepository: WorkItemRepository = mockk()
    private val taigaSessionStorage: KmpTaigaSessionStorage = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val workItemEditStateRepository: WorkItemEditStateRepository = mockk(relaxed = true)

    private lateinit var sut: TaskDetailsViewModel

    @Before
    fun setup() {
        mainDispatcherRule.setup()

        every {
            workItemEditStateRepository.getTeamMemberUpdateFlow(taskId, type)
        } returns flowOf(TeamMemberUpdate.Clear)

        every {
            workItemEditStateRepository.getTagsFlow(taskId, type)
        } returns emptyFlow()

        every {
            workItemEditStateRepository.getDescriptionFlow(taskId, type)
        } returns emptyFlow()
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
            fileUriManager = fileUriManager,
            customFieldsUIMapper = customFieldsUIMapper,
            historyRepository = historyRepository,
            workItemRepository = workItemRepository,
            taigaSessionStorage = taigaSessionStorage,
            usersRepository = usersRepository,
            workItemEditStateRepository = workItemEditStateRepository
        )
    }

    private fun setupSuccessfulLoad() {
        val taskDetailsData = getTaskDetailsData(
            task = getTask(id = taskId)
        )

        coEvery {
            taskDetailsDataUseCase.getTaskData(taskId)
        } returns Result.success(taskDetailsData)

        coEvery {
            customFieldsUIMapper.toUI(any())
        } returns persistentListOf()
    }

    @Test
    fun `initial state should have correct toolbar title`() = runTest {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        assertTrue(state.toolbarTitle is NativeText.Arguments)
        val toolbarTitle = state.toolbarTitle
        assertEquals(RString.task_slug, toolbarTitle.id)
        assertEquals(listOf(ref), toolbarTitle.args)
    }

    @Test
    fun `loadTask success should update state correctly`() = runTest {
        val task = getTask(id = taskId)
        val taskDetailsData = getTaskDetailsData(task = task)

        coEvery {
            taskDetailsDataUseCase.getTaskData(taskId)
        } returns Result.success(taskDetailsData)

        coEvery {
            customFieldsUIMapper.toUI(any())
        } returns persistentListOf()

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
    fun `loadTask failure should update state with error`() = runTest {
        coEvery {
            taskDetailsDataUseCase.getTaskData(taskId)
        } returns Result.failure(testException)

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.initialLoadError !is NativeText.Empty)
    }

    @Test
    fun `setDropdownMenuExpanded should update state`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        assertFalse(sut.state.value.isDropdownMenuExpanded)

        sut.state.value.setDropdownMenuExpanded(true)

        assertTrue(sut.state.value.isDropdownMenuExpanded)

        sut.state.value.setDropdownMenuExpanded(false)

        assertFalse(sut.state.value.isDropdownMenuExpanded)
    }

    @Test
    fun `retryLoadTask should reload data`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.retryLoadTask()

        coVerify(exactly = 2) {
            taskDetailsDataUseCase.getTaskData(taskId)
        }
    }

    @Test
    fun `setIsDeleteDialogVisible should update state`() = runTest {
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
        coEvery {
            taskDetailsDataUseCase.deleteTask(any())
        } returns Result.success(Unit)

        createViewModel()

        sut.deleteTrigger.test {
            sut.state.value.onDelete()

            assertTrue(awaitItem())
        }

        coVerify { taskDetailsDataUseCase.deleteTask(any()) }
    }

    @Test
    fun `onDelete failure should update state and not emit trigger`() = runTest {
        setupSuccessfulLoad()
        coEvery {
            taskDetailsDataUseCase.deleteTask(any())
        } returns Result.failure(testException)

        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        coVerify { taskDetailsDataUseCase.deleteTask(any()) }
    }

    @Test
    fun `onGoingToEditTags should set tags in repository`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditTags()

        verify {
            workItemEditStateRepository.setTags(
                workItemId = taskId,
                type = type,
                tags = any()
            )
        }
    }

    @Test
    fun `onGoingToEditWatchers should set current watchers in repository`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditWatchers()

        verify {
            workItemEditStateRepository.setCurrentWatchers(
                ids = any(),
                workItemId = taskId,
                type = type
            )
        }
    }

    @Test
    fun `onGoingToEditAssignee should set current assignee in repository`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.onGoingToEditAssignee()

        verify {
            workItemEditStateRepository.setCurrentAssignee(
                workItemId = taskId,
                type = type,
                id = any()
            )
        }
    }

    @Test
    fun `promoteToUserStory success should emit trigger`() = runTest {
        setupSuccessfulLoad()
        val workItem: WorkItem = mockk()
        coEvery {
            workItemRepository.promoteToUserStory(any(), CommonTaskType.Task)
        } returns workItem

        createViewModel()

        sut.promotedToUserStoryTrigger.test {
            sut.state.value.onPromoteClick()

            assertEquals(workItem, awaitItem())
        }

        assertFalse(sut.state.value.isLoading)
        coVerify { workItemRepository.promoteToUserStory(any(), CommonTaskType.Task) }
    }

    @Test
    fun `promoteToUserStory failure should show snackbar error`() = runTest {
        setupSuccessfulLoad()
        coEvery {
            workItemRepository.promoteToUserStory(any(), CommonTaskType.Task)
        } throws testException

        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onPromoteClick()

            val message = awaitItem()
            assertTrue(message !is NativeText.Empty)
        }

        assertFalse(sut.state.value.isLoading)
        coVerify { workItemRepository.promoteToUserStory(any(), CommonTaskType.Task) }
    }
}
