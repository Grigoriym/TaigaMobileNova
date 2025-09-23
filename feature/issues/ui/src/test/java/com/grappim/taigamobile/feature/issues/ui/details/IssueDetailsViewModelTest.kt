package com.grappim.taigamobile.feature.issues.ui.details

import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsDataUseCase
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.ui.models.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.SavedStateHandleRule
import com.grappim.taigamobile.testing.getRandomInt
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
    private val statusUIMapper: StatusUIMapper = mockk()
    private val tagUIMapper: TagUIMapper = mockk()
    private val customFieldsUIMapper: CustomFieldsUIMapper = mockk()
    private val workItemsGenerator: WorkItemsGenerator = mockk()
    private val workItemEditShared: WorkItemEditShared = mockk()
    private val dateTimeUtils: DateTimeUtils = mockk()
    private val fileUriManager: FileUriManager = mockk()
    private val session: Session = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()

    private lateinit var viewModel: IssueDetailsViewModel

    @Before
    fun setup() {
        every { workItemEditShared.teamMemberUpdateState } returns flowOf(TeamMemberUpdate.Clear)
        every { workItemEditShared.tagsState } returns flowOf(persistentListOf())
        every { workItemEditShared.descriptionState } returns flowOf(getRandomString())

        every { patchDataGenerator.getTagsPatchPayload(any()) } returns persistentMapOf()
        every { patchDataGenerator.getDescriptionPatchPayload(any()) } returns persistentMapOf()

        loadIssueError()

        viewModel = IssueDetailsViewModel(
            issueDetailsDataUseCase = issueDetailsDataUseCase,
            savedStateHandle = savedStateHandleRule.savedStateHandleMock,
            statusUIMapper = statusUIMapper,
            tagUIMapper = tagUIMapper,
            customFieldsUIMapper = customFieldsUIMapper,
            workItemsGenerator = workItemsGenerator,
            workItemEditShared = workItemEditShared,
            dateTimeUtils = dateTimeUtils,
            fileUriManager = fileUriManager,
            session = session,
            patchDataGenerator = patchDataGenerator
        )
    }

    private fun loadIssueError() {
        coEvery { issueDetailsDataUseCase.getIssueData(taskId, ref) } throws testException
    }

    @Test
    fun `on setDropdownMenuExpanded, should update field`() {
        assertFalse(viewModel.state.value.isDropdownMenuExpanded)

        viewModel.state.value.setDropdownMenuExpanded(true)

        assertTrue(viewModel.state.value.isDropdownMenuExpanded)
    }

    @Test
    fun `on setIsRemoveAssigneeDialogVisible, should update field`() {
        assertFalse(viewModel.state.value.isRemoveAssigneeDialogVisible)

        viewModel.state.value.setIsRemoveAssigneeDialogVisible(true)

        assertTrue(viewModel.state.value.isRemoveAssigneeDialogVisible)
    }

    @Test
    fun `on setIsRemoveWatcherDialogVisible, should update field`() {
        assertFalse(viewModel.state.value.isRemoveWatcherDialogVisible)

        viewModel.state.value.setIsRemoveWatcherDialogVisible(true)

        assertTrue(viewModel.state.value.isRemoveWatcherDialogVisible)
    }

    @Test
    fun `on setIsBlockDialogVisible, should update field`() {
        assertFalse(viewModel.state.value.isBlockDialogVisible)

        viewModel.state.value.setIsBlockDialogVisible(true)

        assertTrue(viewModel.state.value.isBlockDialogVisible)
    }

    @Test
    fun `on setIsDeleteDialogVisible, should update field`() {
        assertFalse(viewModel.state.value.isDeleteDialogVisible)

        viewModel.state.value.setIsDeleteDialogVisible(true)

        assertTrue(viewModel.state.value.isDeleteDialogVisible)
    }

    @Test
    fun `on setAreAttachmentsExpanded, should update field`() {
        assertFalse(viewModel.state.value.areAttachmentsExpanded)

        viewModel.state.value.setAreAttachmentsExpanded(true)

        assertTrue(viewModel.state.value.areAttachmentsExpanded)
    }

    @Test
    fun `on setIsCustomFieldsWidgetExpanded, should update field`() {
        assertFalse(viewModel.state.value.isCustomFieldsWidgetExpanded)

        viewModel.state.value.setIsCustomFieldsWidgetExpanded(true)

        assertTrue(viewModel.state.value.isCustomFieldsWidgetExpanded)
    }

    @Test
    fun `on setIsCommentsWidgetExpanded, should update field`() {
        assertFalse(viewModel.state.value.isCommentsWidgetExpanded)

        viewModel.state.value.setIsCommentsWidgetExpanded(true)

        assertTrue(viewModel.state.value.isCommentsWidgetExpanded)
    }

    @Test
    fun `on onWorkItemBadgeClick, should set active badge`() {
        val badge = mockk<SelectableWorkItemBadgeStatus>()
        assertNull(viewModel.state.value.activeBadge)

        viewModel.state.value.onWorkingItemBadgeClick(badge)

        assertEquals(badge, viewModel.state.value.activeBadge)
    }

    @Test
    fun `on onBadgeSheetDismiss, should clear active badge`() {
        val badge = mockk<SelectableWorkItemBadgeStatus>()
        viewModel.state.value.onWorkingItemBadgeClick(badge)
        assertEquals(badge, viewModel.state.value.activeBadge)

        viewModel.state.value.onBadgeSheetDismiss()

        assertNull(viewModel.state.value.activeBadge)
    }

    @Test
    fun `on onRemoveWatcherClick, should set watcherIdToRemove and show dialog`() {
        val watcherId = getRandomLong()
        assertNull(viewModel.state.value.watcherIdToRemove)
        assertFalse(viewModel.state.value.isRemoveWatcherDialogVisible)

        viewModel.state.value.onRemoveWatcherClick(watcherId)

        assertEquals(watcherId, viewModel.state.value.watcherIdToRemove)
        assertTrue(viewModel.state.value.isRemoveWatcherDialogVisible)
    }

    @Test
    fun `on onRemoveAssigneeClick, should show remove assignee dialog`() {
        assertFalse(viewModel.state.value.isRemoveAssigneeDialogVisible)

        viewModel.state.value.onRemoveAssigneeClick()

        assertTrue(viewModel.state.value.isRemoveAssigneeDialogVisible)
    }

    @Test
    fun `on setDueDateDatePickerVisibility, should set isDueDateDatePickerVisible`() {
        assertFalse(viewModel.state.value.isDueDateDatePickerVisible)

        viewModel.state.value.setIsDueDatePickerVisible(true)

        assertTrue(viewModel.state.value.isDueDateDatePickerVisible)
    }
}
