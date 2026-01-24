package com.grappim.taigamobile.feature.issues.ui.details

import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsDataUseCase
import com.grappim.taigamobile.feature.issues.ui.model.IssueUIMapper
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.SavedStateHandleRule
import com.grappim.taigamobile.testing.getIssueDetailsData
import com.grappim.taigamobile.testing.getIssueUI
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

internal class IssueDetailsViewModelTest {
    private val issueId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.Issue)

    @get:Rule
    val savedStateHandleRule = SavedStateHandleRule(
        IssueDetailsNavDestination(
            issueId = issueId,
            ref = ref
        )
    )

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val issueDetailsDataUseCase: IssueDetailsDataUseCase = mockk()
    private val customFieldsUIMapper: CustomFieldsUIMapper = mockk()
    private val workItemsGenerator: WorkItemsGenerator = mockk()
    private val workItemEditStateRepository: WorkItemEditStateRepository = mockk(relaxed = true)
    private val dateTimeUtils: DateTimeUtils = mockk()
    private val fileUriManager: FileUriManager = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val historyRepository: HistoryRepository = mockk()
    private val workItemRepository: WorkItemRepository = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val issueUIMapper: IssueUIMapper = mockk()

    private lateinit var sut: IssueDetailsViewModel

    @Before
    fun setup() {
        every {
            workItemEditStateRepository.getTeamMemberUpdateFlow(issueId, type)
        } returns flowOf(TeamMemberUpdate.Clear)

        every {
            workItemEditStateRepository.getTagsFlow(issueId, type)
        } returns emptyFlow()

        every {
            workItemEditStateRepository.getDescriptionFlow(issueId, type)
        } returns emptyFlow()

        every {
            workItemEditStateRepository.getSprintFlow(issueId, type)
        } returns emptyFlow()
    }

    private fun createViewModel() {
        sut = IssueDetailsViewModel(
            issueDetailsDataUseCase = issueDetailsDataUseCase,
            savedStateHandle = savedStateHandleRule.savedStateHandleMock,
            customFieldsUIMapper = customFieldsUIMapper,
            workItemsGenerator = workItemsGenerator,
            workItemEditStateRepository = workItemEditStateRepository,
            dateTimeUtils = dateTimeUtils,
            fileUriManager = fileUriManager,
            patchDataGenerator = patchDataGenerator,
            historyRepository = historyRepository,
            workItemRepository = workItemRepository,
            taigaSessionStorage = taigaSessionStorage,
            usersRepository = usersRepository,
            issueUIMapper = issueUIMapper
        )
    }

    private fun setupSuccessfulLoad() {
        val issueDetailsData = getIssueDetailsData()
        val issueUI = getIssueUI()

        coEvery {
            issueDetailsDataUseCase.getIssueData(issueId)
        } returns Result.success(issueDetailsData)

        coEvery {
            issueUIMapper.toUI(any())
        } returns issueUI

        coEvery {
            customFieldsUIMapper.toUI(any())
        } returns persistentListOf()

        coEvery {
            workItemsGenerator.getItems(
                statusUI = any(),
                typeUI = any(),
                severityUI = any(),
                priorityUi = any(),
                filtersData = any()
            )
        } returns persistentSetOf()

        every { dateTimeUtils.formatToMediumFormat(any<LocalDate>()) } returns "Jan 1, 2024"
    }

    @Test
    fun `initial state should have correct toolbar title`() = runTest {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        assertTrue(state.toolbarTitle is NativeText.Arguments)
        val toolbarTitle = state.toolbarTitle
        assertEquals(RString.issue_slug, toolbarTitle.id)
        assertEquals(listOf(ref), toolbarTitle.args)
    }

    @Test
    fun `loadIssue success should update state correctly`() = runTest {
        val issueDetailsData = getIssueDetailsData()
        val issueUI = getIssueUI()

        coEvery {
            issueDetailsDataUseCase.getIssueData(issueId)
        } returns Result.success(issueDetailsData)

        coEvery {
            issueUIMapper.toUI(any())
        } returns issueUI

        coEvery {
            customFieldsUIMapper.toUI(any())
        } returns persistentListOf()

        coEvery {
            workItemsGenerator.getItems(
                statusUI = any(),
                typeUI = any(),
                severityUI = any(),
                priorityUi = any(),
                filtersData = any()
            )
        } returns persistentSetOf()

        every { dateTimeUtils.formatToMediumFormat(any<LocalDate>()) } returns "Jan 1, 2024"

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.initialLoadError)
        assertNotNull(state.currentIssue)
        assertNotNull(state.originalIssue)
        assertEquals(issueDetailsData.sprint, state.sprint)
        assertEquals(issueDetailsData.creator, state.creator)
        assertEquals(issueDetailsData.filtersData, state.filtersData)
        assertEquals(issueDetailsData.canDeleteIssue, state.canDeleteIssue)
        assertEquals(issueDetailsData.canModifyIssue, state.canModifyIssue)
        assertEquals(issueDetailsData.canComment, state.canComment)
    }

    @Test
    fun `loadIssue failure should update state with error`() = runTest {
        coEvery {
            issueDetailsDataUseCase.getIssueData(issueId)
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
    fun `loadIssue should reload data`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.loadIssue()

        coVerify(exactly = 2) {
            issueDetailsDataUseCase.getIssueData(issueId)
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
            workItemRepository.deleteWorkItem(any(), CommonTaskType.Issue)
        } returns Unit

        createViewModel()

        sut.deleteTrigger.test {
            sut.state.value.onDelete()

            assertTrue(awaitItem())
        }

        coVerify { workItemRepository.deleteWorkItem(any(), CommonTaskType.Issue) }
    }

    @Test
    fun `onDelete failure should update state and not emit trigger`() = runTest {
        setupSuccessfulLoad()
        coEvery {
            workItemRepository.deleteWorkItem(any(), CommonTaskType.Issue)
        } throws testException

        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        coVerify { workItemRepository.deleteWorkItem(any(), CommonTaskType.Issue) }
    }

    @Test
    fun `onGoingToEditTags should set tags in repository`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditTags()

        verify {
            workItemEditStateRepository.setTags(
                workItemId = issueId,
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
                workItemId = issueId,
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
                workItemId = issueId,
                type = type,
                id = any()
            )
        }
    }

    @Test
    fun `onGoingToEditSprint should set current sprint in repository`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditSprint()

        verify {
            workItemEditStateRepository.setCurrentSprint(
                workItemId = issueId,
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
            workItemRepository.promoteToUserStory(any(), CommonTaskType.Issue)
        } returns workItem

        createViewModel()

        sut.promotedToUserStoryTrigger.test {
            sut.state.value.onPromoteClick()

            assertEquals(workItem, awaitItem())
        }

        assertFalse(sut.state.value.isLoading)
        coVerify { workItemRepository.promoteToUserStory(any(), CommonTaskType.Issue) }
    }

    @Test
    fun `promoteToUserStory failure should show snackbar error`() = runTest {
        setupSuccessfulLoad()
        coEvery {
            workItemRepository.promoteToUserStory(any(), CommonTaskType.Issue)
        } throws testException

        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onPromoteClick()

            val message = awaitItem()
            assertTrue(message !is NativeText.Empty)
        }

        assertFalse(sut.state.value.isLoading)
        coVerify { workItemRepository.promoteToUserStory(any(), CommonTaskType.Issue) }
    }
}
