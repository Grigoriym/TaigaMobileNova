package com.grappim.taigamobile.feature.tasks.ui

import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.tasks.domain.TaskDetailsDataUseCase
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.SavedStateHandleRule
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getStatusUI
import com.grappim.taigamobile.testing.getTask
import com.grappim.taigamobile.testing.getTaskDetailsData
import com.grappim.taigamobile.testing.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaskDetailsViewModelTest {
    private val taskId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.Task)

    @get:Rule
    val savedStateHandleRule = SavedStateHandleRule(
        TaskDetailsNavDestination(
            taskId = taskId,
            ref = ref
        )
    )

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val taskDetailsDataUseCase: TaskDetailsDataUseCase = mockk()
    private val workItemsGenerator: WorkItemsGenerator = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val statusUIMapper: StatusUIMapper = mockk()
    private val tagUIMapper: TagUIMapper = mockk()
    private val dateTimeUtils: DateTimeUtils = mockk()
    private val fileUriManager: FileUriManager = mockk()
    private val customFieldsUIMapper: CustomFieldsUIMapper = mockk()
    private val historyRepository: HistoryRepository = mockk()
    private val workItemRepository: WorkItemRepository = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val workItemEditStateRepository: WorkItemEditStateRepository = mockk(relaxed = true)

    private lateinit var sut: TaskDetailsViewModel

    @Before
    fun setup() {
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

    private fun createViewModel() {
        sut = TaskDetailsViewModel(
            savedStateHandle = savedStateHandleRule.savedStateHandleMock,
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
            statusUIMapper.toUI(any<Statuses>())
        } returns getStatusUI()

        coEvery {
            tagUIMapper.toUI(any<ImmutableList<Tag>>())
        } returns persistentListOf()

        coEvery {
            workItemsGenerator.getItems(
                statusUI = any(),
                filtersData = any()
            )
        } returns persistentSetOf()

        coEvery {
            customFieldsUIMapper.toUI(any<CustomFields>())
        } returns persistentListOf()

        every { dateTimeUtils.formatToMediumFormat(any<LocalDate>()) } returns "Jan 1, 2024"
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
            statusUIMapper.toUI(any<Statuses>())
        } returns getStatusUI()

        coEvery {
            tagUIMapper.toUI(any<ImmutableList<Tag>>())
        } returns persistentListOf()

        coEvery {
            workItemsGenerator.getItems(
                statusUI = any(),
                filtersData = any()
            )
        } returns persistentSetOf()

        coEvery {
            customFieldsUIMapper.toUI(any<CustomFields>())
        } returns persistentListOf()

        every { dateTimeUtils.formatToMediumFormat(any<LocalDate>()) } returns "Jan 1, 2024"

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
