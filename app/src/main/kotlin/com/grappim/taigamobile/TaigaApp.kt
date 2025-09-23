package com.grappim.taigamobile

import android.app.Application
import android.os.StrictMode
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.data.interceptors.DebugLocalHostImageManager
import com.grappim.taigamobile.utils.FileLoggingTree
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TaigaApp :
    Application(),
    SingletonImageLoader.Factory {

    @Inject
    lateinit var appInfoProvider: AppInfoProvider

    @Inject
    lateinit var debugLocalHostImageManager: DebugLocalHostImageManager

    private var fileLoggingTree: FileLoggingTree? = null

    override fun onCreate() {
        super.onCreate()
        setupStrictMode()

        val minLoggingPriority = if (appInfoProvider.isDebug()) {
            Timber.plant(Timber.DebugTree())
            Log.DEBUG
        } else {
            Log.WARN
        }

        try {
            fileLoggingTree = FileLoggingTree(
                applicationContext.getExternalFilesDir("logs")!!.absolutePath,
                minLoggingPriority
            )
            Timber.plant(fileLoggingTree!!)
        } catch (e: NullPointerException) {
            Timber.e(e, "Cannot setup FileLoggingTree, skipping")
        }
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

    // todo refactor before release
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .apply {
                if (appInfoProvider.isDebug()) {
                    components {
                        add(
                            OkHttpNetworkFetcherFactory(
                                OkHttpClient.Builder()
                                    .addInterceptor(debugLocalHostImageManager)
                                    .build()
                            )
                        )
                    }
                    logger(DebugLogger())
                }
            }
            .crossfade(true)
            .build()
}
