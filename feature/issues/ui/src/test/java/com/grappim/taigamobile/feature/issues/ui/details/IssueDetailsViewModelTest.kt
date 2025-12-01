package com.grappim.taigamobile.feature.issues.ui.details

import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsDataUseCase
import com.grappim.taigamobile.feature.issues.ui.model.IssueUIMapper
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.models.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.SavedStateHandleRule
import com.grappim.taigamobile.testing.getCustomFieldItemState
import com.grappim.taigamobile.testing.getIssueDetailsData
import com.grappim.taigamobile.testing.getIssueUI
import com.grappim.taigamobile.testing.getRandomInt
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getSelectableWorkItemBadgeState
import com.grappim.taigamobile.testing.getStatusUI
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class IssueDetailsViewModelTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val taskId = getRandomLong()
    private val ref = getRandomInt()

    private val route = IssueDetailsNavDestination(taskId, ref)

    @get:Rule
    val savedStateHandleRule = SavedStateHandleRule(route)

    private val issueDetailsDataUseCase: IssueDetailsDataUseCase = mockk()
    private val customFieldsUIMapper: CustomFieldsUIMapper = mockk()
    private val workItemsGenerator: WorkItemsGenerator = mockk()
    private val workItemEditShared: WorkItemEditShared = mockk()
    private val dateTimeUtils: DateTimeUtils = mockk()
    private val fileUriManager: FileUriManager = mockk()
    private val session: Session = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val historyRepository: HistoryRepository = mockk()
    private val workItemRepository: WorkItemRepository = mockk()
    private val taigaStorage: TaigaStorage = mockk()

    private val issueUIMapper: IssueUIMapper = mockk()

    private lateinit var viewModel: IssueDetailsViewModel

    private val issueDetails = getIssueDetailsData()

    @Before
    fun setup() {
        every { dateTimeUtils.formatToMediumFormat(localDate = any()) } returns getRandomString()

        every { workItemEditShared.teamMemberUpdateState } returns flowOf(TeamMemberUpdate.Clear)
        every { workItemEditShared.tagsState } returns flowOf(persistentListOf())
        every { workItemEditShared.descriptionState } returns flowOf(getRandomString())
        every { workItemEditShared.sprintState } returns flowOf(getRandomLong())

        every { patchDataGenerator.getTagsPatchPayload(any()) } returns persistentMapOf()
        every { patchDataGenerator.getDescriptionPatchPayload(any()) } returns persistentMapOf()

        coEvery { issueDetailsDataUseCase.getIssueData(taskId) } returns Result.success(
            issueDetails
        )

        every { session.userId } returns getRandomLong()

        coEvery { issueUIMapper.toUI(issueDetails.issue) } returns getIssueUI()

        val type = getStatusUI()
        val severity = getStatusUI()
        val priority = getStatusUI()
        val status = getStatusUI()

        coEvery { customFieldsUIMapper.toUI(issueDetails.customFields) } returns persistentListOf(
            getCustomFieldItemState()
        )

        coEvery {
            workItemsGenerator.getItems(
                statusUI = status,
                typeUI = type,
                severityUI = severity,
                priorityUi = priority,
                filtersData = issueDetails.filtersData
            )
        } returns persistentSetOf(getSelectableWorkItemBadgeState())

        viewModel = IssueDetailsViewModel(
            issueDetailsDataUseCase = issueDetailsDataUseCase,
            savedStateHandle = savedStateHandleRule.savedStateHandleMock,
            customFieldsUIMapper = customFieldsUIMapper,
            workItemsGenerator = workItemsGenerator,
            workItemEditShared = workItemEditShared,
            dateTimeUtils = dateTimeUtils,
            fileUriManager = fileUriManager,
            session = session,
            patchDataGenerator = patchDataGenerator,
            usersRepository = usersRepository,
            historyRepository = historyRepository,
            workItemRepository = workItemRepository,
            taigaStorage = taigaStorage,
            issueUIMapper = issueUIMapper
        )
    }

    @Test
    fun `on setDropdownMenuExpanded, should update field`() {
        assertFalse(viewModel.state.value.isDropdownMenuExpanded)

        viewModel.state.value.setDropdownMenuExpanded(true)

        assertTrue(viewModel.state.value.isDropdownMenuExpanded)
    }

    @Test
    fun `on setIsDeleteDialogVisible, should update field`() {
        assertFalse(viewModel.state.value.isDeleteDialogVisible)

        viewModel.state.value.setIsDeleteDialogVisible(true)

        assertTrue(viewModel.state.value.isDeleteDialogVisible)
    }
}
