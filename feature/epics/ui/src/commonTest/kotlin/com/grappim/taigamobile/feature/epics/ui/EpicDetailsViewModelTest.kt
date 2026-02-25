@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.epics.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.EpicDetailsDataUseCase
import com.grappim.taigamobile.feature.epics.ui.details.EpicDetailsViewModel
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.WorkItemUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.epic_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getEpic
import com.grappim.taigamobile.testing.models.getEpicDetailsData
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeEpicDetailsDataUseCase
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.FakePatchDataGenerator
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

internal class EpicDetailsViewModelTest {
    private val epicId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.Epic)

    private val savedStateHandle = SavedStateHandle(
        mapOf("epicId" to epicId, "ref" to ref)
    )

    private val mainDispatcherRule = MainDispatcherRule()

    private val statusUIMapper = StatusUIMapper()
    private val tagUIMapper = TagUIMapper()
    private val dateTimeUtils: DateTimeUtils = FakeDateTimeUtils()
    private val epicDetailsDataUseCase = FakeEpicDetailsDataUseCase()
    private val workItemRepository = FakeWorkItemRepository()
    private val patchDataGenerator: PatchDataGenerator = FakePatchDataGenerator()
    private val historyRepository: HistoryRepository = FakeHistoryRepository()
    private val usersRepository: UsersRepository = FakeUsersRepository()
    private val taigaSessionStorage: TaigaSessionStorage = FakeTaigaSessionStorage()
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
            savedStateHandle = savedStateHandle,
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

    private fun setupSuccessfulLoad() {
        epicDetailsDataUseCase.getEpicDataResult = Result.success(
            getEpicDetailsData(epic = getEpic(id = epicId))
        )
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
}
