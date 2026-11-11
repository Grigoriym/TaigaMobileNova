package com.grappim.taigamobile.data

import android.content.Context
import coil3.ImageLoader
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.grappim.kit.appinfo.AppInfoProvider
import com.grappim.taigamobile.core.api.BaseUrlProvider
import com.grappim.taigamobile.core.appinfoapi.DebugLocalHostProvider
import com.grappim.taigamobile.core.storage.customheaders.CustomHeadersStorage
import com.grappim.taigamobile.images.customHeadersNetworkFetcher
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.annotation.Factory

@Factory
class ImageLoaderProvider(
    private val appInfoProvider: AppInfoProvider,
    private val debugLocalHostProvider: DebugLocalHostProvider,
    private val debugLocalHostImageManager: DebugLocalHostImageManager,
    private val customHeadersStorage: CustomHeadersStorage,
    private val baseUrlProvider: BaseUrlProvider,
    private val context: Context
) {

    fun provide(): ImageLoader {
        val rewritesLocalHost = appInfoProvider.isDebug() &&
            debugLocalHostProvider.getDebugLocalHost().isNotEmpty()
        val engine = if (rewritesLocalHost) {
            OkHttp.create { addInterceptor(debugLocalHostImageManager) }
        } else {
            null
        }
        return ImageLoader.Builder(context)
            .components {
                add(customHeadersNetworkFetcher(customHeadersStorage, baseUrlProvider, engine))
            }
            .apply {
                if (appInfoProvider.isDebug()) {
                    logger(DebugLogger())
                }
            }
            .crossfade(true)
            .build()
    }
}
