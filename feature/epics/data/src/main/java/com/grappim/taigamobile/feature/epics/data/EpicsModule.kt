package com.grappim.taigamobile.feature.epics.data

import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface EpicsModule {
    @[Binds Singleton]
    fun bindEpicsRepository(epicsRepository: EpicsRepositoryImpl): EpicsRepository
}
