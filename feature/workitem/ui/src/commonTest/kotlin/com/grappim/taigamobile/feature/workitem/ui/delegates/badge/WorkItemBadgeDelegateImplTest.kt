package com.grappim.taigamobile.feature.workitem.ui.delegates.badge

import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgePriority
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeSeverity
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeType
import com.grappim.taigamobile.testing.models.getSelectableWorkItemBadgeState
import com.grappim.taigamobile.testing.models.getStatusUI
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkItemBadgeDelegateImplTest {

    private lateinit var sut: WorkItemBadgeDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository = FakeWorkItemRepository()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()

    @BeforeTest
    fun setup() {
        workItemRepository.patchDataResult = PatchedData(newVersion = PATCHED_VERSION, dueDateStatus = null)
        sut =
            WorkItemBadgeDelegateImpl(
                commonTaskType = commonTaskType,
                workItemRepository = workItemRepository,
                patchDataGenerator = patchDataGenerator
            )
    }

    private fun state() = sut.badgeState.value

    private fun statusUI(id: Long) = getStatusUI().copy(id = id)

    private fun lastPayload(): Map<String, Any?> = workItemRepository.patchDataCalls.single().payload

    private suspend fun save(
        type: SelectableWorkItemBadgeState,
        item: StatusUI,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit = { }
    ) = sut.handleBadgeSave(
        type = type,
        item = item,
        version = VERSION,
        workItemId = WORK_ITEM_ID,
        doOnPreExecute = doOnPreExecute,
        doOnSuccess = doOnSuccess,
        doOnError = doOnError
    )

    @Test
    fun `on onWorkItemBadgeClick should set active badge`() {
        val badge = getSelectableWorkItemBadgeState()
        assertNull(state().activeBadge)

        state().onBadgeClick(badge)

        assertEquals(badge, state().activeBadge)
    }

    @Test
    fun `on onBadgeSheetDismiss should clear active badge`() {
        val badge = getSelectableWorkItemBadgeState()
        state().onBadgeClick(badge)
        assertEquals(badge, state().activeBadge)

        state().onBadgeSheetDismiss()

        assertNull(state().activeBadge)
    }

    // --- setWorkItemBadges ---

    @Test
    fun `setWorkItemBadges - stores the badges`() {
        val badges = persistentSetOf(statusBadge(), typeBadge())

        sut.setWorkItemBadges(badges)

        assertEquals(badges, state().workItemBadges)
    }

    @Test
    fun `setWorkItemBadges - replaces the previous badges`() {
        sut.setWorkItemBadges(persistentSetOf(statusBadge()))
        val replacement = persistentSetOf(severityBadge())

        sut.setWorkItemBadges(replacement)

        assertEquals(replacement, state().workItemBadges)
    }

    // --- handleBadgeSave: the patch call ---

    @Test
    fun `handleBadgeSave - success - patches with the given version, id and task type`() = runTest {
        val badge = statusBadge()
        sut.setWorkItemBadges(persistentSetOf(badge))

        save(badge, statusUI(NEW_VALUE_ID))

        val call = workItemRepository.patchDataCalls.single()
        assertEquals(VERSION, call.version)
        assertEquals(WORK_ITEM_ID, call.workItemId)
        assertEquals(commonTaskType, call.commonTaskType)
    }

    /**
     * The four `payload` arms and the four `badge.copy(currentValue = item)` arms of the success path
     * are keyed off the same badge subtype, so each of these tests covers one of each: the payload
     * key the generator produced, and the badge whose `currentValue` was replaced in state.
     */
    @Test
    fun `handleBadgeSave - success - a status badge sends status and takes the new value`() = runTest {
        val badge = statusBadge()
        val newValue = statusUI(NEW_VALUE_ID)
        sut.setWorkItemBadges(persistentSetOf(badge))

        save(badge, newValue)

        assertEquals(mapOf<String, Any?>("status" to NEW_VALUE_ID), lastPayload())
        assertEquals(newValue, state().workItemBadges.single().currentValue)
    }

    @Test
    fun `handleBadgeSave - success - a type badge sends type and takes the new value`() = runTest {
        val badge = typeBadge()
        val newValue = statusUI(NEW_VALUE_ID)
        sut.setWorkItemBadges(persistentSetOf(badge))

        save(badge, newValue)

        assertEquals(mapOf<String, Any?>("type" to NEW_VALUE_ID), lastPayload())
        assertEquals(newValue, state().workItemBadges.single().currentValue)
    }

    @Test
    fun `handleBadgeSave - success - a severity badge sends severity and takes the new value`() = runTest {
        val badge = severityBadge()
        val newValue = statusUI(NEW_VALUE_ID)
        sut.setWorkItemBadges(persistentSetOf(badge))

        save(badge, newValue)

        assertEquals(mapOf<String, Any?>("severity" to NEW_VALUE_ID), lastPayload())
        assertEquals(newValue, state().workItemBadges.single().currentValue)
    }

    @Test
    fun `handleBadgeSave - success - a priority badge sends priority and takes the new value`() = runTest {
        val badge = priorityBadge()
        val newValue = statusUI(NEW_VALUE_ID)
        sut.setWorkItemBadges(persistentSetOf(badge))

        save(badge, newValue)

        assertEquals(mapOf<String, Any?>("priority" to NEW_VALUE_ID), lastPayload())
        assertEquals(newValue, state().workItemBadges.single().currentValue)
    }

    // --- handleBadgeSave: success ---

    @Test
    fun `handleBadgeSave - success - invokes doOnPreExecute and doOnSuccess with the new version`() = runTest {
        val badge = statusBadge()
        sut.setWorkItemBadges(persistentSetOf(badge))
        var preExecuteCalled = false
        var newVersion: Long? = null

        save(
            type = badge,
            item = statusUI(NEW_VALUE_ID),
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = { newVersion = it }
        )

        assertTrue(preExecuteCalled)
        assertEquals(PATCHED_VERSION, newVersion)
    }

    @Test
    fun `handleBadgeSave - success - null doOnPreExecute and doOnSuccess are optional`() = runTest {
        val badge = statusBadge()
        sut.setWorkItemBadges(persistentSetOf(badge))

        save(type = badge, item = statusUI(NEW_VALUE_ID), doOnPreExecute = null, doOnSuccess = null)

        assertEquals(1, workItemRepository.patchDataCalls.size)
    }

    @Test
    fun `handleBadgeSave - success - clears the active badge and stops updating`() = runTest {
        val badge = statusBadge()
        sut.setWorkItemBadges(persistentSetOf(badge))
        state().onBadgeClick(badge)

        save(badge, statusUI(NEW_VALUE_ID))

        assertNull(state().activeBadge)
        assertTrue(state().updatingBadges.isEmpty())
    }

    @Test
    fun `handleBadgeSave - success - marks the badge as updating while the patch is in flight`() = runTest {
        val badge = statusBadge()
        sut.setWorkItemBadges(persistentSetOf(badge))

        sut.badgeState.test {
            assertTrue(awaitItem().updatingBadges.isEmpty())

            save(badge, statusUI(NEW_VALUE_ID))

            assertEquals(persistentSetOf(badge), awaitItem().updatingBadges)
            assertTrue(awaitItem().updatingBadges.isEmpty())
        }
    }

    @Test
    fun `handleBadgeSave - success - leaves the other badges untouched`() = runTest {
        val saved = statusBadge()
        val other = typeBadge()
        sut.setWorkItemBadges(persistentSetOf(saved, other))

        save(saved, statusUI(NEW_VALUE_ID))

        assertEquals(other, state().workItemBadges.last())
    }

    /**
     * Unreachable from the UI — the badge passed in is always one that was rendered from
     * `workItemBadges` — but it is the `else` arm of the `badge == type` check, and it documents that
     * an unknown badge patches the work item without being added to the set.
     */
    @Test
    fun `handleBadgeSave - success - a badge that is not in the set changes nothing in state`() = runTest {
        val known = statusBadge()
        sut.setWorkItemBadges(persistentSetOf(known))

        save(typeBadge(), statusUI(NEW_VALUE_ID))

        assertEquals(persistentSetOf(known), state().workItemBadges)
        assertEquals(1, workItemRepository.patchDataCalls.size)
    }

    // --- handleBadgeSave: failure ---

    @Test
    fun `handleBadgeSave - failure - invokes doOnError and stops updating`() = runTest {
        workItemRepository.patchDataThrows = testException
        val badge = statusBadge()
        sut.setWorkItemBadges(persistentSetOf(badge))
        state().onBadgeClick(badge)
        var caught: Throwable? = null

        save(type = badge, item = statusUI(NEW_VALUE_ID), doOnError = { caught = it })

        assertEquals(testException.message, caught?.message)
        assertNull(state().activeBadge)
        assertTrue(state().updatingBadges.isEmpty())
    }

    @Test
    fun `handleBadgeSave - failure - keeps the badge's previous value`() = runTest {
        workItemRepository.patchDataThrows = testException
        val previousValue = statusUI(OLD_VALUE_ID)
        val badge = statusBadge(previousValue)
        sut.setWorkItemBadges(persistentSetOf(badge))

        save(badge, statusUI(NEW_VALUE_ID))

        assertEquals(previousValue, state().workItemBadges.single().currentValue)
    }

    @Test
    fun `handleBadgeSave - failure - doOnPreExecute ran but doOnSuccess did not`() = runTest {
        workItemRepository.patchDataThrows = testException
        val badge = statusBadge()
        sut.setWorkItemBadges(persistentSetOf(badge))
        var preExecuteCalled = false
        var successCalled = false

        save(
            type = badge,
            item = statusUI(NEW_VALUE_ID),
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = { successCalled = true }
        )

        assertTrue(preExecuteCalled)
        assertFalse(successCalled)
    }

    /**
     * `resultOf` rethrows cancellation instead of turning it into a `Result.failure`, so a cancelled
     * save must not reach `doOnError`. The badge is left in `updatingBadges` — correct, since the
     * work item's state is unknown at that point.
     */
    @Test
    fun `handleBadgeSave - a cancellation propagates and does not reach doOnError`() = runTest {
        workItemRepository.patchDataThrows = CancellationException("cancelled")
        val badge = statusBadge()
        sut.setWorkItemBadges(persistentSetOf(badge))
        var errorCalled = false

        assertFailsWith<CancellationException> {
            save(type = badge, item = statusUI(NEW_VALUE_ID), doOnError = { errorCalled = true })
        }

        assertFalse(errorCalled)
        assertEquals(persistentSetOf(badge), state().updatingBadges)
    }

    private fun statusBadge(current: StatusUI = statusUI(OLD_VALUE_ID)) = SelectableWorkItemBadgeStatus(
        options = persistentListOf(current),
        currentValue = current
    )

    private fun typeBadge(current: StatusUI = statusUI(OLD_VALUE_ID)) = SelectableWorkItemBadgeType(
        options = persistentListOf(current),
        currentValue = current
    )

    private fun severityBadge(current: StatusUI = statusUI(OLD_VALUE_ID)) = SelectableWorkItemBadgeSeverity(
        options = persistentListOf(current),
        currentValue = current
    )

    private fun priorityBadge(current: StatusUI = statusUI(OLD_VALUE_ID)) = SelectableWorkItemBadgePriority(
        options = persistentListOf(current),
        currentValue = current
    )

    private companion object {
        const val VERSION = 3L
        const val WORK_ITEM_ID = 111L
        const val PATCHED_VERSION = 4L
        const val OLD_VALUE_ID = 10L
        const val NEW_VALUE_ID = 20L
    }
}
