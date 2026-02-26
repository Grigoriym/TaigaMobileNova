@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.userstories.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.userstory_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getUserStory
import com.grappim.taigamobile.testing.models.getUserStoryDetailsData
import com.grappim.taigamobile.testing.repo.FakeEpicsRepository
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeUserStoryDetailsDataUseCase
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.formatter.decimal.createDecimalFormatter
import com.grappim.taigamobile.utils.ui.NativeText
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
    private val historyRepository: HistoryRepository = FakeHistoryRepository()
    private val workItemRepository: WorkItemRepository = FakeWorkItemRepository()
    private val taigaSessionStorage: TaigaSessionStorage = FakeTaigaSessionStorage()
    private val usersRepository: UsersRepository = FakeUsersRepository()
    private val epicsRepository: EpicsRepository = FakeEpicsRepository()
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

    private fun setupSuccessfulLoad() {
        val userStory = getUserStory(id = userStoryId)
        userStoryDetailsDataUseCase.getUserStoryDataResult = Result.success(
            getUserStoryDetailsData(userStory = userStory)
        )
        userStoryDetailsDataUseCase.getUserStoryResult = userStory
    }

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
}
