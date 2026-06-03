package com.grappim.taigamobile

import android.app.Application
import android.os.StrictMode
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.asynckmp.ApplicationScope
import com.grappim.taigamobile.core.logger.TimberLogger
import com.grappim.taigamobile.core.storage.cache.CacheManager
import com.grappim.taigamobile.data.ImageLoaderProvider
import com.grappim.taigamobile.di.KoinApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.plugin.module.dsl.startKoin
import org.koin.plugin.module.dsl.typeQualifier
import timber.log.Timber

class TaigaApp :
    Application(),
    SingletonImageLoader.Factory {

    private val appInfoProvider: AppInfoProvider by inject()

    private val imageLoaderProvider: ImageLoaderProvider by inject()

    private val cacheManager: CacheManager by inject()

    private val applicationScope: CoroutineScope by inject(typeQualifier(ApplicationScope::class))

    override fun onCreate() {
        super.onCreate()

        startKoin<KoinApp> {
            androidContext(this@TaigaApp)
            printLogger()
        }

        setupStrictMode()
        setupLogger()

        // Clean expired cache on app start
        applicationScope.launch {
            cacheManager.cleanExpiredCache()
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoaderProvider.provide()

    private fun setupLogger() {
        if (appInfoProvider.isDebug()) {
            Timber.plant(Timber.DebugTree())
        }
        TimberLogger.install()
    }

    private fun setupStrictMode() {
        if (appInfoProvider.isDebug()) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
}
