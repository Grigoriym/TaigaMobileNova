package com.grappim.taigamobile.login.data

import com.grappim.taigamobile.BuildConfig
import com.grappim.taigamobile.data.api.NetworkConstants
import com.grappim.taigamobile.state.Session
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Invocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenInterceptor @Inject constructor(
    private val session: Session
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()

        requestBuilder.putUserAgent()

        val annotation = getAnnotation(request)
        if (annotation == null) {
            requestBuilder.putXownerAndAuthorization()
        }
        return chain.proceed(requestBuilder.build())
    }

    private fun getAnnotation(request: Request): RequestWithoutAuthToken? =
        request.tag(Invocation::class.java)
            ?.method()?.getAnnotation(RequestWithoutAuthToken::class.java)

    private fun Request.Builder.putUserAgent() {
        val userAgentValue = "TaigaMobileNova/${BuildConfig.VERSION_NAME}"
        this@putUserAgent.header(
            NetworkConstants.USER_AGENT,
            userAgentValue
        )
    }

    private fun Request.Builder.putXownerAndAuthorization() {
        val bearerToken = "Bearer ${session.token.value}"
        this@putXownerAndAuthorization.header(
            NetworkConstants.AUTHORIZATION,
            bearerToken
        )
    }
}
