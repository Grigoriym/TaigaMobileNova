package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface SprintsModule {

    @[Binds Singleton]
    fun bindSprintsRepository(sprintsRepositoryImpl: SprintsRepositoryImpl): SprintsRepository
}
