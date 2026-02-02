package com.grappim.taigamobile

import android.app.Application
import android.os.StrictMode
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.data.ImageLoaderProvider
import dagger.hilt.android.HiltAndroidApp
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

    override fun onCreate() {
        super.onCreate()
        setupStrictMode()

        if (appInfoProvider.isDebug()) {
            Timber.plant(Timber.DebugTree())
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
