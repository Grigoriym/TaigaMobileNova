package com.grappim.taigamobile.feature.workitem.ui.delegates.attachments

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemAttachmentsDelegateImplTest {

    private lateinit var sut: WorkItemAttachmentsDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val taigaStorage: TaigaStorage = mockk()
    private val fileUriManager: FileUriManager = mockk()

    @Before
    fun setup() {
        sut = WorkItemAttachmentsDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            taigaStorage = taigaStorage,
            fileUriManager = fileUriManager
        )
    }

    @Test
    fun `on setAreAttachmentsExpanded, should update field`() {
        assertFalse(sut.attachmentsState.value.areAttachmentsExpanded)

        sut.attachmentsState.value.setAreAttachmentsExpanded(true)

        assertTrue(sut.attachmentsState.value.areAttachmentsExpanded)
    }
}
