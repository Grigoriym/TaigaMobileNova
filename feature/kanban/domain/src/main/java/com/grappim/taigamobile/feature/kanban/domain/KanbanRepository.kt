package com.grappim.taigamobile.feature.kanban.domain

interface KanbanRepository {
    suspend fun getData(): Result<KanbanData>
}
