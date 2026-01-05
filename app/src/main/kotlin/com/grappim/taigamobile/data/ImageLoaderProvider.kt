package com.grappim.taigamobile.data

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.data.interceptors.DebugLocalHostImageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class ImageLoaderProvider @Inject constructor(
    private val appInfoProvider: AppInfoProvider,
    private val debugLocalHostImageManager: DebugLocalHostImageManager,
    @ApplicationContext private val context: Context
) {

    fun provide(): ImageLoader = ImageLoader.Builder(context)
        .apply {
            if (appInfoProvider.isDebug()) {
                components {
                    if (appInfoProvider.getDebugLocalHost().isNotEmpty()) {
                        add(
                            OkHttpNetworkFetcherFactory(
                                OkHttpClient.Builder()
                                    .addInterceptor(debugLocalHostImageManager)
                                    .build()
                            )
                        )
                    }
                }
                logger(DebugLogger())
            }
        }
        .crossfade(true)
        .build()
}
