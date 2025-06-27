package com.grappim.taigamobile.feature.dashboard.data

import com.grappim.taigamobile.feature.dashboard.domain.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface DashboardModule {
    @[Binds Singleton]
    fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
