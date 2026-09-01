package com.grappim.taigamobile.core.asynckmp

import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
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

private val applicationScopeExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    logcat(LogPriority.ERROR, throwable = throwable) { "Unhandled exception on ApplicationScope" }
}

@Module
@Configuration
@ComponentScan("com.grappim.taigamobile.core.asynckmp")
class KmpCoroutinesModule {

    @[Single DefaultDispatcher]
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @[Single IoDispatcher]
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @[Single ApplicationScope]
    fun provideApplicationScope(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisorJob() + defaultDispatcher + applicationScopeExceptionHandler)

    @[Single MainDispatcher]
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @[Single MainImmediateDispatcher]
    fun providesMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}
