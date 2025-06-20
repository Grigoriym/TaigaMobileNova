package com.grappim.taigamobile.feature.login.data.di

import com.grappim.taigamobile.feature.login.data.repo.AuthRepositoryImpl
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface LoginModule {
    @Singleton
    @Binds
    fun bindIAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository
}
