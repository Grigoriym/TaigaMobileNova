package com.grappim.taigamobile.core.storage.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

const val SPRINT_TABLE = "sprint_table"

@Entity(
    tableName = SPRINT_TABLE,
    indices = [Index("projectId")]
)
data class SprintEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val projectId: Long,
    val name: String,
    val order: Int,
    val start: LocalDate,
    val end: LocalDate,
    val storiesCount: Int,
    val isClosed: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)
