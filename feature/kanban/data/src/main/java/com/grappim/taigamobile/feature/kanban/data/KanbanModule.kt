package com.grappim.taigamobile.feature.kanban.data

import com.grappim.taigamobile.feature.kanban.domain.KanbanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface KanbanModule {
    @[Binds Singleton]
    fun bindKanbanRepository(impl: KanbanRepositoryImpl): KanbanRepository
}
