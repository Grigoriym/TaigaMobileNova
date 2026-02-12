package com.grappim.taigamobile.core.storage.db.entities

import androidx.room.Entity
import androidx.room.Index
import com.grappim.taigamobile.core.domain.CommonTaskType
import java.time.LocalDateTime

const val WORK_ITEM_TABLE = "work_item_table"

/**
 * Cached work item for list display (dashboard, backlog, etc.).
 * Uses composite primary key since same ID can exist across different task types.
 */
@Entity(
    tableName = WORK_ITEM_TABLE,
    primaryKeys = ["id", "taskType"],
    indices = [Index("projectId"), Index("sprintId")]
)
data class WorkItemEntity(
    val id: Long,
    val taskType: CommonTaskType,
    val projectId: Long,
    val projectName: String,
    val ref: Long,
    val title: String,
    val createdDate: LocalDateTime,
    val isClosed: Boolean,
    val isBlocked: Boolean,
    val blockedNote: String?,
    // Status flattened
    val statusId: Long,
    val statusName: String,
    val statusColor: String,
    // Assignee flattened (nullable)
    val assigneeId: Long?,
    val assigneeName: String?,
    val assigneePhoto: String?,
    // Tags as JSON list of "name|color" strings
    val tagsJson: String,
    // Colors (epic colors) as JSON list
    val colorsJson: String,
    // Sprint/milestone ID for filtering
    val sprintId: Long?,
    // Cache metadata
    val cachedAt: Long = System.currentTimeMillis()
)
