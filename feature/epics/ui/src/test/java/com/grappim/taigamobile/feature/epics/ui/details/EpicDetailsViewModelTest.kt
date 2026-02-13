package com.grappim.taigamobile.feature.epics.ui.details

import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.EpicDetailsDataUseCase
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.WorkItemUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.SavedStateHandleRule
import com.grappim.taigamobile.testing.getEpic
import com.grappim.taigamobile.testing.getEpicDetailsData
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getStatusUI
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EpicDetailsViewModelTest {
    private val epicId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.Epic)

    @get:Rule
    val savedStateHandleRule = SavedStateHandleRule(
        EpicDetailsNavDestination(
            epicId = epicId,
            ref = ref
        )
    )

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val historyRepository: HistoryRepository = mockk()
    private val fileUriManager: FileUriManager = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val dateTimeUtils: DateTimeUtils = mockk()
    private val epicDetailsDataUseCase: EpicDetailsDataUseCase = mockk()
    private val statusUIMapper: StatusUIMapper = mockk()
    private val workItemsGenerator: WorkItemsGenerator = mockk()
    private val tagUIMapper: TagUIMapper = mockk()
    private val customFieldsUIMapper: CustomFieldsUIMapper = mockk()
    private val workItemUIMapper: WorkItemUIMapper = mockk()
    private val workItemEditStateRepository: WorkItemEditStateRepository = mockk(relaxed = true)

    private lateinit var sut: EpicDetailsViewModel

    @Before
    fun setup() {
        every {
            workItemEditStateRepository.getTeamMemberUpdateFlow(epicId, type)
        } returns flowOf(TeamMemberUpdate.Clear)

        every {
            workItemEditStateRepository.getTagsFlow(epicId, type)
        } returns emptyFlow()

        every {
            workItemEditStateRepository.getDescriptionFlow(epicId, type)
        } returns emptyFlow()
    }

    private fun createViewModel() {
        sut = EpicDetailsViewModel(
            savedStateHandle = savedStateHandleRule.savedStateHandleMock,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator,
            historyRepository = historyRepository,
            fileUriManager = fileUriManager,
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
        val epicDetailsData = getEpicDetailsData(
            epic = getEpic(id = epicId)
        )

        coEvery {
            epicDetailsDataUseCase.getEpicData(epicId)
        } returns Result.success(epicDetailsData)

        coEvery {
            statusUIMapper.toUI(any<Statuses>())
        } returns getStatusUI()

        coEvery {
            tagUIMapper.toSelectableUI(any<ImmutableList<Tag>>())
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

        coEvery {
            workItemUIMapper.toUI(any<ImmutableList<WorkItem>>())
        } returns persistentListOf()
    }

    @Test
    fun `initial state should have correct toolbar title`() = runTest {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        assertTrue(state.toolbarTitle is NativeText.Arguments)
        val toolbarTitle = state.toolbarTitle
        assertEquals(RString.epic_slug, toolbarTitle.id)
        assertEquals(listOf(ref), toolbarTitle.args)
    }

    @Test
    fun `loadEpic success should update state correctly`() = runTest {
        val epic = getEpic(id = epicId)
        val epicDetailsData = getEpicDetailsData(epic = epic)

        coEvery {
            epicDetailsDataUseCase.getEpicData(epicId)
        } returns Result.success(epicDetailsData)

        coEvery {
            statusUIMapper.toUI(any<Statuses>())
        } returns getStatusUI()

        coEvery {
            tagUIMapper.toSelectableUI(any<ImmutableList<Tag>>())
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

        coEvery {
            workItemUIMapper.toUI(any<ImmutableList<WorkItem>>())
        } returns persistentListOf()

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
    fun `loadEpic failure should update state with error`() = runTest {
        coEvery {
            epicDetailsDataUseCase.getEpicData(epicId)
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
    fun `retryLoadEpic should reload data`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.retryLoadEpic()

        coVerify(exactly = 2) {
            epicDetailsDataUseCase.getEpicData(epicId)
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
            workItemRepository.deleteWorkItem(any(), CommonTaskType.Epic)
        } returns Unit

        createViewModel()

        sut.deleteTrigger.test {
            sut.state.value.onDelete()

            assertTrue(awaitItem())
        }

        coVerify { workItemRepository.deleteWorkItem(any(), CommonTaskType.Epic) }
    }

    @Test
    fun `onDelete failure should update state and not emit trigger`() = runTest {
        setupSuccessfulLoad()
        coEvery {
            workItemRepository.deleteWorkItem(any(), CommonTaskType.Epic)
        } throws testException

        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        coVerify { workItemRepository.deleteWorkItem(any(), CommonTaskType.Epic) }
    }

    @Test
    fun `setAreWorkItemsExpanded should update state`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        assertFalse(sut.state.value.areWorkItemsExpanded)

        sut.state.value.setAreWorkItemsExpanded(true)

        assertTrue(sut.state.value.areWorkItemsExpanded)

        sut.state.value.setAreWorkItemsExpanded(false)

        assertFalse(sut.state.value.areWorkItemsExpanded)
    }

    @Test
    fun `onGoingToEditTags should set tags in repository`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditTags()

        verify {
            workItemEditStateRepository.setTags(
                workItemId = epicId,
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
                workItemId = epicId,
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
                workItemId = epicId,
                type = type,
                id = any()
            )
        }
    }
}
