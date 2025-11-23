package com.grappim.taigamobile.feature.workitem.ui.delegates.badge

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkItemBadgeDelegateImplTest {

    private lateinit var sut: WorkItemBadgeDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()

    @Before
    fun setup() {
        sut = WorkItemBadgeDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator
        )
    }

    @Test
    fun `on onWorkItemBadgeClick, should set active badge`() {
        val badge = mockk<SelectableWorkItemBadgeStatus>()
        assertNull(sut.badgeState.value.activeBadge)

        sut.badgeState.value.onBadgeClick(badge)

        assertEquals(badge, sut.badgeState.value.activeBadge)
    }

    @Test
    fun `on onBadgeSheetDismiss, should clear active badge`() {
        val badge = mockk<SelectableWorkItemBadgeStatus>()
        sut.badgeState.value.onBadgeClick(badge)
        assertEquals(badge, sut.badgeState.value.activeBadge)

        sut.badgeState.value.onBadgeSheetDismiss()

        assertNull(sut.badgeState.value.activeBadge)
    }
}
