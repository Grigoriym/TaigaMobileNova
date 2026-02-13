package com.grappim.taigamobile

import android.app.Application
import android.os.StrictMode
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.async.ApplicationScope
import com.grappim.taigamobile.core.storage.cache.CacheManager
import com.grappim.taigamobile.data.ImageLoaderProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TaigaApp :
    Application(),
    SingletonImageLoader.Factory {

    @Inject
    lateinit var appInfoProvider: AppInfoProvider

    @Inject
    lateinit var imageLoaderProvider: ImageLoaderProvider

    @Inject
    lateinit var cacheManager: CacheManager

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        setupStrictMode()

        if (appInfoProvider.isDebug()) {
            Timber.plant(Timber.DebugTree())
        }

        // Clean expired cache on app start
        applicationScope.launch {
            cacheManager.cleanExpiredCache()
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoaderProvider.provide()

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
