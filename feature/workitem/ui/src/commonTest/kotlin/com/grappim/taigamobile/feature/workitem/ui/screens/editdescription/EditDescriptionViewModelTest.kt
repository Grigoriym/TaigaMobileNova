package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class EditDescriptionViewModelTest {

    private val workItemId = getRandomLong()
    private val description = getRandomString()

    private val workItemEditStateRepository = WorkItemEditStateRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: EditDescriptionViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel(taskIdentifier: TaskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)) {
        sut = EditDescriptionViewModel(
            workItemEditStateRepository = workItemEditStateRepository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "description" to description,
                    "workItemId" to workItemId,
                    "taskIdentifier" to Json.encodeToString(taskIdentifier)
                )
            )
        )
    }

    // --- init ---

    @Test
    fun `on init both descriptions are taken from the route`() {
        createViewModel()

        assertEquals(description, sut.state.value.originalDescription)
        assertEquals(description, sut.state.value.currentDescription)
    }

    @Test
    fun `on init the dialog is hidden`() {
        createViewModel()

        assertFalse(sut.state.value.isDialogVisible)
    }

    // --- onDescriptionChange ---

    @Test
    fun `onDescriptionChange replaces the current description and keeps the original`() {
        createViewModel()
        val newValue = getRandomString()

        sut.state.value.onDescriptionChange(newValue)

        assertEquals(newValue, sut.state.value.currentDescription)
        assertEquals(description, sut.state.value.originalDescription)
    }

    // --- dialog visibility ---

    @Test
    fun `setIsDialogVisible true shows the dialog`() {
        createViewModel()

        sut.state.value.setIsDialogVisible(true)

        assertTrue(sut.state.value.isDialogVisible)
    }

    @Test
    fun `setIsDialogVisible false hides the dialog`() {
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.state.value.setIsDialogVisible(false)

        assertFalse(sut.state.value.isDialogVisible)
    }

    // --- shouldGoBackWithCurrentValue ---

    @Test
    fun `shouldGoBackWithCurrentValue emits the back action`() = runTest {
        createViewModel()

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }
    }

    @Test
    fun `shouldGoBackWithCurrentValue hides the dialog before navigating back`() = runTest {
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }

        assertFalse(sut.state.value.isDialogVisible)
    }

    /**
     * `getDescriptionFlow` is a rendezvous channel, so the collector has to be waiting before
     * `updateDescription` sends — hence the `launch { … take(1) }` before the back action is
     * triggered.
     */
    @Test
    fun `shouldGoBackWithCurrentValue true and a changed description sends it to the repository`() = runTest {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        createViewModel(taskIdentifier)
        val newValue = getRandomString()
        sut.state.value.onDescriptionChange(newValue)

        var received: String? = null
        val collectJob = launch {
            workItemEditStateRepository.getDescriptionFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.join()
        assertEquals(newValue, received)
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and an unchanged description sends nothing`() = runTest {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        createViewModel(taskIdentifier)

        var received: String? = null
        val collectJob = launch {
            workItemEditStateRepository.getDescriptionFlow(workItemId, taskIdentifier)
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
    fun `shouldGoBackWithCurrentValue false sends nothing even when the description changed`() = runTest {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        createViewModel(taskIdentifier)
        sut.state.value.onDescriptionChange(getRandomString())

        var received: String? = null
        val collectJob = launch {
            workItemEditStateRepository.getDescriptionFlow(workItemId, taskIdentifier)
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

    /**
     * The wiki session key is derived differently from a work item's, so the description has to
     * reach the flow keyed by [TaskIdentifier.Wiki] rather than a work-item session.
     */
    @Test
    fun `shouldGoBackWithCurrentValue true sends the description for a wiki page`() = runTest {
        createViewModel(TaskIdentifier.Wiki)
        val newValue = getRandomString()
        sut.state.value.onDescriptionChange(newValue)

        var received: String? = null
        val collectJob = launch {
            workItemEditStateRepository.getDescriptionFlow(workItemId, TaskIdentifier.Wiki)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.join()
        assertEquals(newValue, received)
    }
}
