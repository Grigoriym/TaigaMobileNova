package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface HistoryModule {

    @[Binds Singleton]
    fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
}
