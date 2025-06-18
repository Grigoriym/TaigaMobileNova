package com.grappim.taigamobile.userstories

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface UserStoriesModule {

    @Binds
    @Singleton
    fun bindUserStoriesRepository(impl: UserStoriesImpl): UserStoriesRepository
}
