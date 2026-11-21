package com.grappim.taigamobile.images

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import com.grappim.kit.customheaders.CustomHeader
import com.grappim.kit.customheaders.CustomHeadersStorage
import com.grappim.kit.testing.FakeCustomHeadersStorage
import com.grappim.taigamobile.core.api.BaseUrlProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

// Desktop/JVM: drives Coil's real Ktor fetcher with a MockEngine (or the JVM's own engine for
// newImageLoader). The Android image loader is built elsewhere and is not covered here.
class ImageNetworkFetcherJvmTest {

    private val storage = FakeCustomHeadersStorage(listOf(TOKEN))
    private val baseUrlProvider = object : BaseUrlProvider {
        override fun getBaseUrl(): String = SERVER_URL
    }
    private val requests = mutableListOf<HttpRequestData>()

    private val engine = MockEngine { request ->
        requests += request
        respond(content = "not an image")
    }

    private val loader = ImageLoader.Builder(PlatformContext.INSTANCE)
        .memoryCache(null)
        .diskCache(null)
        .components { add(customHeadersNetworkFetcher(storage, baseUrlProvider, engine)) }
        .build()

    @Test
    fun `image from the server's own origin carries the custom headers`() = runTest {
        loader.execute(ImageRequest.Builder(PlatformContext.INSTANCE).data("$SERVER_URL/media/a.png").build())

        assertEquals(TOKEN.value, requests.single().headers[TOKEN.name])
    }

    @Test
    fun `off-origin image gets no custom headers`() = runTest {
        loader.execute(ImageRequest.Builder(PlatformContext.INSTANCE).data(GRAVATAR_URL).build())

        assertNull(requests.single().headers[TOKEN.name])
    }

    @Test
    fun `newImageLoader resolves its collaborators from Koin and fetches with the platform engine`() = runTest {
        startKoin {
            modules(
                module {
                    single<CustomHeadersStorage> { storage }
                    single<BaseUrlProvider> { baseUrlProvider }
                }
            )
        }
        try {
            val result = newImageLoader(PlatformContext.INSTANCE).execute(
                ImageRequest.Builder(PlatformContext.INSTANCE).data(UNREACHABLE_URL).build()
            )

            assertIs<ErrorResult>(result)
        } finally {
            stopKoin()
        }
    }

    private companion object {
        const val SERVER_URL = "http://taiga.example.com"
        const val GRAVATAR_URL = "https://www.gravatar.com/avatar/abc.png"

        // Port 1 on loopback refuses the connection at once, so the real engine fails fast.
        const val UNREACHABLE_URL = "http://127.0.0.1:1/a.png"
        val TOKEN = CustomHeader(name = "X-Proxy-Token", value = "secret")
    }
}
