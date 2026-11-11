package com.grappim.taigamobile.images

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.NetworkFetcher
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.grappim.taigamobile.core.api.BaseUrlProvider
import com.grappim.taigamobile.core.api.CustomHeaders
import com.grappim.taigamobile.core.storage.customheaders.CustomHeadersStorage
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.mp.KoinPlatform

/**
 * Coil's network fetcher for every platform. Image loads don't go through the app's API clients,
 * so behind an authenticating reverse proxy they need the user's custom headers too, or avatars
 * and attachments served by the server are refused while the data around them loads.
 * [CustomHeaders] sends them only to the server's own origin, so off-origin images (Gravatar) get
 * nothing.
 *
 * [engine] is null to let Ktor pick the platform's engine from the classpath.
 */
fun customHeadersNetworkFetcher(
    customHeadersStorage: CustomHeadersStorage,
    baseUrlProvider: BaseUrlProvider,
    engine: HttpClientEngine? = null
): NetworkFetcher.Factory = KtorNetworkFetcherFactory(
    httpClient = {
        val config: HttpClientConfig<*>.() -> Unit = {
            install(CustomHeaders) {
                storage = customHeadersStorage
                serverUrl = { baseUrlProvider.getBaseUrl() }
            }
        }
        if (engine != null) HttpClient(engine, config) else HttpClient(config)
    }
)

/**
 * The app-wide image loader on desktop and iOS, installed at startup before the first `AsyncImage`
 * would make Coil build its own default. Needs Koin to be started.
 */
fun newImageLoader(context: PlatformContext): ImageLoader {
    val koin = KoinPlatform.getKoin()
    return ImageLoader.Builder(context)
        .components { add(customHeadersNetworkFetcher(koin.get(), koin.get())) }
        .build()
}
