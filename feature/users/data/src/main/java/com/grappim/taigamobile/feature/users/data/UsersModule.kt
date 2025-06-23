package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.core.api.CommonRetrofit
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface UsersModule {
    @[Binds Singleton]
    fun bindUsersRepository(usersRepositoryImpl: UsersRepositoryImpl): UsersRepository
}

@[Module InstallIn(SingletonComponent::class)]
object UsersProvidesModule {
    @[Provides Singleton]
    fun provideUsersApi(@CommonRetrofit retrofit: Retrofit): UsersApi =
        retrofit.create(UsersApi::class.java)
}
