package com.grappim.taigamobile.feature.workitem.ui.delegates.comments

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemCommentsDelegateImplTest {

    private lateinit var sut: WorkItemCommentsDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val historyRepository: HistoryRepository = mockk()
    private val workItemRepository: WorkItemRepository = mockk()

    @Before
    fun setup() {
        sut = WorkItemCommentsDelegateImpl(
            commonTaskType = commonTaskType,
            historyRepository = historyRepository,
            workItemRepository = workItemRepository
        )
    }

    @Test
    fun `on setIsCommentsWidgetExpanded, should update field`() {
        assertFalse(sut.commentsState.value.isCommentsWidgetExpanded)

        sut.commentsState.value.setIsCommentsWidgetExpanded(true)

        assertTrue(sut.commentsState.value.isCommentsWidgetExpanded)
    }
}
