package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.Session
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor that adds a token to requests automatically
 */
@Singleton
class AuthTokenProviderInterceptor @Inject constructor(
    private val session: Session,
    private val appInfoProvider: AppInfoProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()

        requestBuilder.putUserAgent()
        requestBuilder.putXownerAndAuthorization()

        return chain.proceed(requestBuilder.build())
    }

    private fun Request.Builder.putUserAgent() {
        val userAgentValue = "TaigaMobileNova/${appInfoProvider.getVersionName()}"
        this@putUserAgent.header(
            ApiConstants.USER_AGENT,
            userAgentValue
        )
    }

    private fun Request.Builder.putXownerAndAuthorization() {
        val bearerToken = "Bearer ${session.token.value}"
        this@putXownerAndAuthorization.header(
            ApiConstants.AUTHORIZATION,
            bearerToken
        )
    }
}
