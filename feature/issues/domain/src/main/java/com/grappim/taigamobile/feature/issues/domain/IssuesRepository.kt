package com.grappim.taigamobile.feature.issues.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.collections.immutable.PersistentMap
import kotlinx.coroutines.flow.Flow

interface IssuesRepository {

    suspend fun watchIssue(issueId: Long)
    suspend fun unwatchIssue(issueId: Long)

    suspend fun deleteIssue(id: Long)

    suspend fun addAttachment(issueId: Long, fileName: String, fileByteArray: ByteArray): Attachment

    suspend fun deleteAttachment(attachment: Attachment)

    suspend fun patchData(
        version: Long,
        issueId: Long,
        payload: PersistentMap<String, Any?>
    ): PatchedData

    suspend fun patchCustomAttributes(
        version: Long,
        issueId: Long,
        payload: PersistentMap<String, Any?>
    ): PatchedCustomAttributes

    fun getIssuesPaging(filtersDataDTO: FiltersDataDTO): Flow<PagingData<CommonTask>>

    fun refreshIssues()

    suspend fun getIssueByRef(ref: Int, filtersData: FiltersData): IssueTask

    suspend fun getCustomFields(commonTaskId: Long): CustomFields

    suspend fun getIssueAttachments(taskId: Long): List<Attachment>

    suspend fun createIssue(title: String, description: String, sprintId: Long?): CommonTaskResponse

    suspend fun getIssues(
        isClosed: Boolean = false,
        assignedIds: String? = null,
        watcherId: Long? = null,
        project: Long? = null,
        sprint: Long? = null
    ): List<CommonTask>
}
