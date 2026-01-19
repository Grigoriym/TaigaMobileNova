package com.grappim.taigamobile.core.storage.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission

const val PROJECT_TABLE = "project_table"

@Entity(
    tableName = PROJECT_TABLE
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val name: String,
    val slug: String,
    val myPermissions: List<TaigaPermission>,
    val isEpicsActivated: Boolean,
    val isBacklogActivated: Boolean,
    val isKanbanActivated: Boolean,
    val isIssuesActivated: Boolean,
    val isWikiActivated: Boolean,
    val defaultSwimlane: Long?
)
