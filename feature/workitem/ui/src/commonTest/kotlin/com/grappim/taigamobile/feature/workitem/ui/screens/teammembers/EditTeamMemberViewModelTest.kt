package com.grappim.taigamobile.feature.workitem.ui.screens.teammembers

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.workitem.ui.mappers.TeamMemberUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getTeamMember
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class EditTeamMemberViewModelTest {

    private val workItemId = getRandomLong()
    private val taskIdentifier: TaskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
    private val taskIdentifierJson = Json.encodeToString(taskIdentifier)

    private val usersRepository = FakeUsersRepository()
    private val teamMemberUIMapper = TeamMemberUIMapper()
    private val workItemEditStateRepository = WorkItemEditStateRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private val savedStateHandle = SavedStateHandle(
        mapOf(
            "workItemId" to workItemId,
            "taskIdentifier" to taskIdentifierJson
        )
    )

    private lateinit var sut: EditTeamMemberViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = EditTeamMemberViewModel(
            usersRepository = usersRepository,
            teamMemberUIMapper = teamMemberUIMapper,
            workItemEditStateRepository = workItemEditStateRepository,
            savedStateHandle = savedStateHandle
        )
    }

    // --- init / getUsers ---

    @Test
    fun `on init assignee mode users are loaded into state`() {
        val member = getTeamMember()
        usersRepository.getTeamMembersResult = persistentListOf(member)
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)

        createViewModel()

        assertEquals(1, sut.state.value.itemsToShow.size)
        assertEquals(member.id, sut.state.value.itemsToShow[0].id)
    }

    @Test
    fun `on init watchers mode users are loaded into state`() {
        val member = getTeamMember()
        usersRepository.getTeamMembersResult = persistentListOf(member)
        workItemEditStateRepository.setCurrentWatchers(workItemId, taskIdentifier, persistentListOf())

        createViewModel()

        assertEquals(1, sut.state.value.itemsToShow.size)
    }

    @Test
    fun `on init assignees mode users are loaded into state`() {
        val member = getTeamMember()
        usersRepository.getTeamMembersResult = persistentListOf(member)
        workItemEditStateRepository.setCurrentAssignees(workItemId, taskIdentifier, persistentListOf())

        createViewModel()

        assertEquals(1, sut.state.value.itemsToShow.size)
    }

    @Test
    fun `on init failure itemsToShow remains empty`() {
        usersRepository.getTeamMembersThrows = testException
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)

        createViewModel()

        assertTrue(sut.state.value.itemsToShow.isEmpty())
    }

    // --- retrieveSelectedItems ---

    @Test
    fun `on init assignee mode selectedItems loaded from repository`() {
        val assigneeId = getRandomLong()
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, assigneeId)

        createViewModel()

        assertTrue(sut.state.value.selectedItems.contains(assigneeId))
        assertTrue(sut.state.value.originalSelectedItems.contains(assigneeId))
    }

    @Test
    fun `on init assignee mode selectedItems empty when no assignee set`() {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)

        createViewModel()

        assertTrue(sut.state.value.selectedItems.isEmpty())
    }

    @Test
    fun `on init watchers mode selectedItems loaded from repository`() {
        val watcherId = getRandomLong()
        workItemEditStateRepository.setCurrentWatchers(workItemId, taskIdentifier, persistentListOf(watcherId))

        createViewModel()

        assertTrue(sut.state.value.selectedItems.contains(watcherId))
        assertTrue(sut.state.value.originalSelectedItems.contains(watcherId))
    }

    @Test
    fun `on init assignees mode selectedItems loaded from repository`() {
        val assigneeId = getRandomLong()
        workItemEditStateRepository.setCurrentAssignees(workItemId, taskIdentifier, persistentListOf(assigneeId))

        createViewModel()

        assertTrue(sut.state.value.selectedItems.contains(assigneeId))
        assertTrue(sut.state.value.originalSelectedItems.contains(assigneeId))
    }

    // --- onTeamMemberClick - Assignee mode ---

    @Test
    fun `onTeamMemberClick in assignee mode selects member`() {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()
        val memberId = getRandomLong()

        sut.state.value.onTeamMemberClick(memberId)

        assertEquals(1, sut.state.value.selectedItems.size)
        assertTrue(sut.state.value.selectedItems.contains(memberId))
    }

    @Test
    fun `onTeamMemberClick in assignee mode replaces previous selection`() {
        val originalId = getRandomLong()
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, originalId)
        createViewModel()
        val newId = getRandomLong()

        sut.state.value.onTeamMemberClick(newId)

        assertEquals(1, sut.state.value.selectedItems.size)
        assertTrue(sut.state.value.selectedItems.contains(newId))
        assertFalse(sut.state.value.selectedItems.contains(originalId))
    }

    @Test
    fun `onTeamMemberClick in assignee mode deselects when clicking already selected member`() {
        val memberId = getRandomLong()
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, memberId)
        createViewModel()

        sut.state.value.onTeamMemberClick(memberId)

        assertTrue(sut.state.value.selectedItems.isEmpty())
    }

    // --- onTeamMemberClick - Watchers mode ---

    @Test
    fun `onTeamMemberClick in watchers mode adds member to selection`() {
        workItemEditStateRepository.setCurrentWatchers(workItemId, taskIdentifier, persistentListOf())
        createViewModel()
        val memberId = getRandomLong()

        sut.state.value.onTeamMemberClick(memberId)

        assertTrue(sut.state.value.selectedItems.contains(memberId))
    }

    @Test
    fun `onTeamMemberClick in watchers mode removes member when already selected`() {
        val memberId = getRandomLong()
        workItemEditStateRepository.setCurrentWatchers(workItemId, taskIdentifier, persistentListOf(memberId))
        createViewModel()

        sut.state.value.onTeamMemberClick(memberId)

        assertFalse(sut.state.value.selectedItems.contains(memberId))
    }

    @Test
    fun `onTeamMemberClick in watchers mode allows multiple selections`() {
        workItemEditStateRepository.setCurrentWatchers(workItemId, taskIdentifier, persistentListOf())
        createViewModel()
        val id1 = getRandomLong()
        val id2 = getRandomLong()

        sut.state.value.onTeamMemberClick(id1)
        sut.state.value.onTeamMemberClick(id2)

        assertEquals(2, sut.state.value.selectedItems.size)
    }

    // --- onTeamMemberClick - Assignees mode ---

    @Test
    fun `onTeamMemberClick in assignees mode adds member to selection`() {
        workItemEditStateRepository.setCurrentAssignees(workItemId, taskIdentifier, persistentListOf())
        createViewModel()
        val memberId = getRandomLong()

        sut.state.value.onTeamMemberClick(memberId)

        assertTrue(sut.state.value.selectedItems.contains(memberId))
    }

    @Test
    fun `onTeamMemberClick in assignees mode removes member when already selected`() {
        val memberId = getRandomLong()
        workItemEditStateRepository.setCurrentAssignees(workItemId, taskIdentifier, persistentListOf(memberId))
        createViewModel()

        sut.state.value.onTeamMemberClick(memberId)

        assertFalse(sut.state.value.selectedItems.contains(memberId))
    }

    @Test
    fun `onTeamMemberClick in assignees mode allows multiple selections`() {
        workItemEditStateRepository.setCurrentAssignees(workItemId, taskIdentifier, persistentListOf())
        createViewModel()
        val id1 = getRandomLong()
        val id2 = getRandomLong()

        sut.state.value.onTeamMemberClick(id1)
        sut.state.value.onTeamMemberClick(id2)

        assertEquals(2, sut.state.value.selectedItems.size)
    }

    // --- isItemSelected ---

    @Test
    fun `isItemSelected returns true for selected member`() {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()
        val memberId = getRandomLong()
        sut.state.value.onTeamMemberClick(memberId)

        assertTrue(sut.state.value.isItemSelected(memberId))
    }

    @Test
    fun `isItemSelected returns false for unselected member`() {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()

        assertFalse(sut.state.value.isItemSelected(getRandomLong()))
    }

    // --- setIsDialogVisible ---

    @Test
    fun `setIsDialogVisible true shows dialog`() {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()

        sut.state.value.setIsDialogVisible(true)

        assertTrue(sut.state.value.isDialogVisible)
    }

    @Test
    fun `setIsDialogVisible false hides dialog`() {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.state.value.setIsDialogVisible(false)

        assertFalse(sut.state.value.isDialogVisible)
    }

    // --- shouldGoBackWithCurrentValue ---

    @Test
    fun `shouldGoBackWithCurrentValue emits back action`() = runTest {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }
    }

    @Test
    fun `shouldGoBackWithCurrentValue hides dialog before navigating back`() = runTest {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }

        assertFalse(sut.state.value.isDialogVisible)
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and changed assignee sends update to repository`() = runTest {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()
        val memberId = getRandomLong()
        sut.state.value.onTeamMemberClick(memberId)

        var received: TeamMemberUpdate? = null
        val collectJob = launch {
            workItemEditStateRepository.getTeamMemberUpdateFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.join()
        val update = assertNotNull(received)
        assertIs<TeamMemberUpdate.Assignee>(update)
        assertEquals(memberId, update.id)
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and changed watchers sends update to repository`() = runTest {
        workItemEditStateRepository.setCurrentWatchers(workItemId, taskIdentifier, persistentListOf())
        createViewModel()
        val memberId = getRandomLong()
        sut.state.value.onTeamMemberClick(memberId)

        var received: TeamMemberUpdate? = null
        val collectJob = launch {
            workItemEditStateRepository.getTeamMemberUpdateFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.join()
        val update = assertNotNull(received)
        assertIs<TeamMemberUpdate.Watchers>(update)
        assertTrue(update.ids.contains(memberId))
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and changed assignees sends update to repository`() = runTest {
        workItemEditStateRepository.setCurrentAssignees(workItemId, taskIdentifier, persistentListOf())
        createViewModel()
        val memberId = getRandomLong()
        sut.state.value.onTeamMemberClick(memberId)

        var received: TeamMemberUpdate? = null
        val collectJob = launch {
            workItemEditStateRepository.getTeamMemberUpdateFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.join()
        val update = assertNotNull(received)
        assertIs<TeamMemberUpdate.Assignees>(update)
        assertTrue(update.ids.contains(memberId))
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and unchanged state does not send update`() = runTest {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()

        var received: TeamMemberUpdate? = null
        val collectJob = launch {
            workItemEditStateRepository.getTeamMemberUpdateFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.cancel()
        assertNull(received)
    }

    @Test
    fun `shouldGoBackWithCurrentValue false does not send update even when state changed`() = runTest {
        workItemEditStateRepository.setCurrentAssignee(workItemId, taskIdentifier, null)
        createViewModel()
        val memberId = getRandomLong()
        sut.state.value.onTeamMemberClick(memberId)

        var received: TeamMemberUpdate? = null
        val collectJob = launch {
            workItemEditStateRepository.getTeamMemberUpdateFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }

        collectJob.cancel()
        assertNull(received)
    }
}
