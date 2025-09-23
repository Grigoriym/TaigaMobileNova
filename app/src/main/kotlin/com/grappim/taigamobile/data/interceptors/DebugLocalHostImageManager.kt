package com.grappim.taigamobile.data.interceptors

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * While debugging I use the locally run Taiga, which has the "localhost" as a host
 * I need to change that to "10.0.2.2" or any other
 * Why? Because images are downloaded with "localhost" as a host, while I need another host
 */
@Singleton
class DebugLocalHostImageManager @Inject constructor(private val appInfoProvider: AppInfoProvider) :
    Interceptor {

    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(lock) {
            val request = chain.request()

            if (!request.url.host.contains("localhost")) return chain.proceed(request)

            val newHost = appInfoProvider.getDebugLocalHost().toHttpUrlOrNull()
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
