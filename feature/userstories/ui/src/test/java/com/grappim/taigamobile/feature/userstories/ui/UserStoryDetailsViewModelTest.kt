package com.grappim.taigamobile.feature.userstories.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoryDetailsDataUseCase
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.userstory_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getStatusUI
import com.grappim.taigamobile.testing.models.getUserStory
import com.grappim.taigamobile.testing.models.getUserStoryDetailsData
import com.grappim.taigamobile.testing.repo.FakeEpicsRepository
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.formatter.decimal.createDecimalFormatter
import com.grappim.taigamobile.utils.ui.NativeText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
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

    private val statusUIMapper: StatusUIMapper = StatusUIMapper()

    private val workItemsGenerator: WorkItemsGenerator = WorkItemsGenerator(
        dispatcher = UnconfinedTestDispatcher(),
        statusUIMapper = statusUIMapper
    )
    private val workItemEditStateRepository: WorkItemEditStateRepository = WorkItemEditStateRepository()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()

    private val tagUIMapper: TagUIMapper = TagUIMapper()
    private val dateTimeUtils: DateTimeUtils = FakeDateTimeUtils()
    private val customFieldsUIMapper: CustomFieldsUIMapper = CustomFieldsUIMapper(createDecimalFormatter())
    private val historyRepository: HistoryRepository = FakeHistoryRepository()
    private val workItemRepository: WorkItemRepository = FakeWorkItemRepository()
    private val taigaSessionStorage: TaigaSessionStorage = FakeTaigaSessionStorage()
    private val usersRepository: UsersRepository = FakeUsersRepository()
    private val epicsRepository: EpicsRepository = FakeEpicsRepository()
    private val userStoryDetailsDataUseCase: UserStoryDetailsDataUseCase = UserStoryDetailsDataUseCase()

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
        val userStoryDetailsData = getUserStoryDetailsData(
            userStory = getUserStory(id = userStoryId)
        )
    }

    @Test
    fun `initial state should have correct toolbar title`() = runTest {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        assertTrue(state.toolbarTitle is NativeText.Arguments)
        val toolbarTitle = state.toolbarTitle
        assertEquals(RString.userstory_slug, toolbarTitle.id)
        assertEquals(listOf(ref), toolbarTitle.args)
    }

    @Test
    fun `loadUserStory success should update state correctly`() = runTest {
        val userStory = getUserStory(id = userStoryId)
        val userStoryDetailsData = getUserStoryDetailsData(userStory = userStory)

        coEvery {
            userStoryDetailsDataUseCase.getUserStoryData(userStoryId)
        } returns Result.success(userStoryDetailsData)

        coEvery {
            statusUIMapper.toUI(any<Statuses>())
        } returns getStatusUI()

        coEvery {
            tagUIMapper.toSelectableUI(any<ImmutableList<Tag>>())
        } returns persistentListOf<SelectableTagUI>()

        coEvery {
            workItemsGenerator.getItems(
                statusUI = any(),
                filtersData = any()
            )
        } returns persistentSetOf()

        coEvery {
            customFieldsUIMapper.toUI(any<CustomFields>())
        } returns persistentListOf<CustomFieldItemState>()

        every { dateTimeUtils.formatToMediumFormat(any<LocalDate>()) } returns "Jan 1, 2024"

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
    fun `retryLoadUserStory should reload data`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.retryLoadUserStory()

        coVerify(exactly = 2) {
            userStoryDetailsDataUseCase.getUserStoryData(userStoryId)
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
            userStoryDetailsDataUseCase.deleteUserStory(userStoryId)
        } returns Result.success(Unit)

        createViewModel()

        sut.deleteTrigger.test {
            sut.state.value.onDelete()

            assertTrue(awaitItem())
        }

        coVerify { userStoryDetailsDataUseCase.deleteUserStory(userStoryId) }
    }

    @Test
    fun `onDelete failure should update state and not emit trigger`() = runTest {
        setupSuccessfulLoad()
        coEvery {
            userStoryDetailsDataUseCase.deleteUserStory(userStoryId)
        } returns Result.failure(testException)

        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        coVerify { userStoryDetailsDataUseCase.deleteUserStory(userStoryId) }
    }

    @Test
    fun `onEditTags should set tags in repository`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onEditTags()

        verify {
            workItemEditStateRepository.setTags(
                workItemId = userStoryId,
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
                workItemId = userStoryId,
                type = type
            )
        }
    }

    @Test
    fun `onGoingToEditAssignees should set current assignees in repository`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.onGoingToEditAssignees()

        verify {
            workItemEditStateRepository.setCurrentAssignees(
                ids = any(),
                workItemId = userStoryId,
                type = type
            )
        }
    }

    @Test
    fun `onGoingToEditEpics should set current epics in repository`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditEpics()

        verify {
            workItemEditStateRepository.setCurrentEpics(
                ids = any(),
                workItemId = userStoryId,
                type = type
            )
        }
    }
}
