package com.grappim.taigamobile.feature.login.data.di

import com.grappim.taigamobile.feature.login.data.repo.AuthRepository
import com.grappim.taigamobile.feature.login.domain.repo.IAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface LoginModule {
    @Singleton
    @Binds
    fun bindIAuthRepository(authRepository: AuthRepository): IAuthRepository
}
