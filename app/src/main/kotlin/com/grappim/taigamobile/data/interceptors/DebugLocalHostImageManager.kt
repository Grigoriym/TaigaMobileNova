package com.grappim.taigamobile.data.interceptors

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * While debugging I use the locally run Taiga, which has the "localhost" as a host
 * I need to change that to "10.0.2.2"
 * Why? Because images are downloaded with "localhost" as a host, while I need another host
 */
@Singleton
class DebugLocalHostImageManager @Inject constructor() : Interceptor {

    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(lock) {
            val request = chain.request()
            val newHost = "http://10.0.2.2:9000/".toHttpUrlOrNull()
            if (newHost != null) {
                val newUrl = request.url.newBuilder()
                    .scheme(newHost.scheme)
                    .host(newHost.toUrl().toURI().host)
                    .port(newHost.port)
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
