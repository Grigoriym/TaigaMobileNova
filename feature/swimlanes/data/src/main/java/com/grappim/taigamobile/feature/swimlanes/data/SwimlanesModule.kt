package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface SwimlanesModule {
    @[Binds Singleton]
    fun bindSwimlanesRepository(impl: SwimlanesRepositoryImpl): SwimlanesRepository
}
