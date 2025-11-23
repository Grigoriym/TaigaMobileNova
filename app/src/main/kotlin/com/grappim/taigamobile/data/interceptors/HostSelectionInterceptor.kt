package com.grappim.taigamobile.data.interceptors

import com.grappim.taigamobile.core.api.BaseUrlProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dynamic usage of the host interceptor
 */
@Singleton
class HostSelectionInterceptor @Inject constructor(private val baseUrlProvider: BaseUrlProvider) : Interceptor {

    private val lock = Any()

    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        synchronized(lock) {
            val request = chain.request()
            val host = baseUrlProvider.getBaseUrl().toHttpUrlOrNull()
            if (host != null) {
                val newUrl = request.url.newBuilder()
                    .scheme(host.scheme)
                    .host(host.toUrl().toURI().host)
                    .port(host.port)
                    .build()
                val newRequest = request.newBuilder()
                    .url(newUrl)
                    .build()
                return chain.proceed(newRequest)
            } else {
                return chain.proceed(request)
            }
        }
    }
}
