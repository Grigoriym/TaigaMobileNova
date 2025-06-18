package com.grappim.taigamobile.core.appinfo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@[Module InstallIn(SingletonComponent::class)]
interface AppInfoModule {
    @Binds
    fun bindAppInfoProvider(appInfoProviderImpl: AppInfoProviderImpl): AppInfoProvider
}
