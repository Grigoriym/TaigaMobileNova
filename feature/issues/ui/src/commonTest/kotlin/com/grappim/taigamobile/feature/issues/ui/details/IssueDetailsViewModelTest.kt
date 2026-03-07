package com.grappim.taigamobile.feature.issues.ui.details

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.issues.ui.model.IssueUIMapper
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.issue_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getIssueDetailsData
import com.grappim.taigamobile.testing.models.getIssueTask
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeIssueDetailsDataUseCase
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.formatter.decimal.createDecimalFormatter
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class IssueDetailsViewModelTest {
    private val issueId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.Issue)

    private val savedStateHandle = SavedStateHandle(
        mapOf("issueId" to issueId, "ref" to ref)
    )

    private val mainDispatcherRule = MainDispatcherRule()

    private val statusUIMapper = StatusUIMapper()
    private val tagUIMapper = TagUIMapper()
    private val issueUIMapper = IssueUIMapper(
        statusUIMapper = statusUIMapper,
        tagUIMapper = tagUIMapper
    )
    private val dateTimeUtils = FakeDateTimeUtils()
    private val issueDetailsDataUseCase = FakeIssueDetailsDataUseCase()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val historyRepository: HistoryRepository = FakeHistoryRepository()
    private val workItemRepository = FakeWorkItemRepository()
    private val taigaSessionStorage: TaigaSessionStorage = FakeTaigaSessionStorage()
    private val usersRepository: UsersRepository = FakeUsersRepository()
    private val workItemsGenerator = WorkItemsGenerator(
        dispatcher = UnconfinedTestDispatcher(),
        statusUIMapper = statusUIMapper
    )
    private val customFieldsUIMapper = CustomFieldsUIMapper(dfSimple = createDecimalFormatter())
    private val workItemEditStateRepository = WorkItemEditStateRepository()

    private lateinit var sut: IssueDetailsViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = IssueDetailsViewModel(
            savedStateHandle = savedStateHandle,
            issueDetailsDataUseCase = issueDetailsDataUseCase,
            customFieldsUIMapper = customFieldsUIMapper,
            workItemsGenerator = workItemsGenerator,
            workItemEditStateRepository = workItemEditStateRepository,
            dateTimeUtils = dateTimeUtils,
            patchDataGenerator = patchDataGenerator,
            historyRepository = historyRepository,
            workItemRepository = workItemRepository,
            taigaSessionStorage = taigaSessionStorage,
            usersRepository = usersRepository,
            issueUIMapper = issueUIMapper
        )
    }

    private fun setupSuccessfulLoad() {
        issueDetailsDataUseCase.getIssueDataResult = Result.success(
            getIssueDetailsData(issue = getIssueTask(id = issueId))
        )
    }

    @Test
    fun `initial state should have correct toolbar title`() {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        val toolbarTitle = state.toolbarTitle as NativeText.Arguments
        assertEquals(RString.issue_slug, toolbarTitle.stringResource)
        assertEquals(listOf(ref), toolbarTitle.args)
    }

    @Test
    fun `loadIssue success should update state correctly`() {
        val issueDetailsData = getIssueDetailsData(issue = getIssueTask(id = issueId))
        issueDetailsDataUseCase.getIssueDataResult = Result.success(issueDetailsData)

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
    fun `loadIssue failure should update state with error`() {
        issueDetailsDataUseCase.getIssueDataResult = Result.failure(testException)

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
    fun `loadIssue should reload data`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.loadIssue()

        assertEquals(2, issueDetailsDataUseCase.getIssueDataCallCount)
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
    fun `onGoingToEditTags should set tags in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditTags()

        assertTrue(workItemEditStateRepository.getCurrentTags(issueId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditWatchers should set current watchers in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditWatchers()

        assertTrue(workItemEditStateRepository.getCurrentWatchers(issueId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditAssignee should set current assignee in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.onGoingToEditAssignee()

        assertNotNull(workItemEditStateRepository.getCurrentAssignee(issueId, type))
    }

    @Test
    fun `onGoingToEditSprint should set current sprint in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditSprint()

        assertNotNull(workItemEditStateRepository.getCurrentSprint(issueId, type))
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
}
