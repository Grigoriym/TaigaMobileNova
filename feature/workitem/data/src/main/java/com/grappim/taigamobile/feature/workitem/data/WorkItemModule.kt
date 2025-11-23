package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface WorkItemModule {
    @[Binds Singleton]
    fun bindWorkItemRepository(impl: WorkItemRepositoryImpl): WorkItemRepository

    @Binds
    fun bindPatchDataGenerator(impl: PatchDataGeneratorImpl): PatchDataGenerator
}
