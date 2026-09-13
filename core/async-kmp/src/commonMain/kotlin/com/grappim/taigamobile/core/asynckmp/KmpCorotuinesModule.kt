package com.grappim.taigamobile.core.asynckmp

import com.grappim.kit.coroutines.KitDispatchers
import com.grappim.kit.coroutines.applicationScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier(name = "ApplicationScope")
annotation class ApplicationScope

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainImmediateDispatcher

@Module
@Configuration
@ComponentScan("com.grappim.taigamobile.core.asynckmp")
class KmpCoroutinesModule {

    @[Single DefaultDispatcher]
    fun providesDefaultDispatcher(): CoroutineDispatcher = KitDispatchers.default

    @[Single IoDispatcher]
    fun providesIoDispatcher(): CoroutineDispatcher = KitDispatchers.io

    @[Single ApplicationScope]
    fun provideApplicationScope(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher): CoroutineScope =
        applicationScope(defaultDispatcher)

    @[Single MainDispatcher]
    fun providesMainDispatcher(): CoroutineDispatcher = KitDispatchers.main

    @[Single MainImmediateDispatcher]
    fun providesMainImmediateDispatcher(): CoroutineDispatcher = KitDispatchers.mainImmediate
}
