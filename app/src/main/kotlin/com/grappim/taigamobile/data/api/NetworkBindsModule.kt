package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.api.BaseUrlProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface NetworkBindsModule {

    @[Binds Singleton]
    fun bindBaseUrlProvider(impl: BaseUrlProviderImpl): BaseUrlProvider
}
