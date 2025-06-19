package com.grappim.taigamobile.core.asyncandroid

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainImmediateDispatcher

@[Module InstallIn(SingletonComponent::class)]
object AndroidCoroutinesModule {

    @[Provides MainDispatcher]
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @[Provides MainImmediateDispatcher]
    fun providesMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}
