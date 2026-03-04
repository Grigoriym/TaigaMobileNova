package com.grappim.taigamobile.core.asynckmp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainImmediateDispatcher

@Module
@Configuration
@ComponentScan
class KmpAndroidCoroutinesModule {

    @[Single MainDispatcher]
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @[Single MainImmediateDispatcher]
    fun providesMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}
