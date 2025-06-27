package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface FiltersModule {

    @[Binds Singleton]
    fun bindFiltersRepository(impl: FiltersRepositoryImpl): FiltersRepository
}
