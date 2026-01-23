package com.grappim.taigamobile.feature.userstories.ui

import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoryDetailsDataUseCase
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.SavedStateHandleRule
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getStatusUI
import com.grappim.taigamobile.testing.getUserStory
import com.grappim.taigamobile.testing.getUserStoryDetailsData
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

class UserStoryDetailsViewModelTest {
    private val userStoryId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.UserStory)

    @get:Rule
    val savedStateHandleRule = SavedStateHandleRule(
        UserStoryDetailsNavDestination(
            userStoryId = userStoryId,
            ref = ref
        )
    )

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userStoryDetailsDataUseCase: UserStoryDetailsDataUseCase = mockk()
    private val workItemsGenerator: WorkItemsGenerator = mockk()
    private val workItemEditStateRepository: WorkItemEditStateRepository = mockk(relaxed = true)
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
    private val epicsRepository: EpicsRepository = mockk()

    private lateinit var sut: UserStoryDetailsViewModel

    @Before
    fun setup() {
        every {
            workItemEditStateRepository.getTeamMemberUpdateFlow(userStoryId, type)
        } returns flowOf(TeamMemberUpdate.Clear)

        every {
            workItemEditStateRepository.getTagsFlow(userStoryId, type)
        } returns flowOf(persistentListOf())

        every {
            workItemEditStateRepository.getDescriptionFlow(userStoryId, type)
        } returns flowOf("")

        every {
            workItemEditStateRepository.getEpicsFlow(userStoryId, type)
        } returns flowOf(persistentListOf())
    }

    private fun createViewModel() {
        sut = UserStoryDetailsViewModel(
            savedStateHandle = savedStateHandleRule.savedStateHandleMock,
            userStoryDetailsDataUseCase = userStoryDetailsDataUseCase,
            workItemsGenerator = workItemsGenerator,
            workItemEditStateRepository = workItemEditStateRepository,
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
            epicsRepository = epicsRepository
        )
    }

    private fun setupSuccessfulLoad() {
        val userStoryDetailsData = getUserStoryDetailsData(
            userStory = getUserStory(id = userStoryId)
        )
        coEvery {
            userStoryDetailsDataUseCase.getUserStoryData(userStoryId)
        } returns Result.success(userStoryDetailsData)

        coEvery {
            statusUIMapper.toUI(any<com.grappim.taigamobile.feature.filters.domain.model.Statuses>())
        } returns getStatusUI()

        coEvery {
            tagUIMapper.toUI(any<ImmutableList<Tag>>())
        } returns persistentListOf<TagUI>()

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
    }

    @Test
    fun `initial state should have correct toolbar title`() = runTest {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        assertTrue(state.toolbarTitle is NativeText.Arguments)
        val toolbarTitle = state.toolbarTitle as NativeText.Arguments
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
            statusUIMapper.toUI(any<com.grappim.taigamobile.feature.filters.domain.model.Statuses>())
        } returns getStatusUI()

        coEvery {
            tagUIMapper.toUI(any<ImmutableList<Tag>>())
        } returns persistentListOf<TagUI>()

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
