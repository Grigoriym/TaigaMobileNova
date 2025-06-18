package com.grappim.taigamobile.login.data

import com.grappim.taigamobile.data.api.NetworkConstants
import com.grappim.taigamobile.data.api.RefreshTokenRequest
import com.grappim.taigamobile.data.api.RefreshTokenRequestJsonAdapter
import com.grappim.taigamobile.data.api.RefreshTokenResponseJsonAdapter
import com.grappim.taigamobile.data.api.TaigaApi
import com.grappim.taigamobile.state.Session
import com.squareup.moshi.Moshi
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaigaAuthenticator @Inject constructor(
    val session: Session,
    val moshi: Moshi,
    val okHttpClient: OkHttpClient,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val currentToken = response.request.header(NetworkConstants.AUTHORIZATION) ?: return null

        try {
            // prevent multiple refresh requests from different threads
            synchronized(session) {
                // refresh token only if it was not refreshed in another thread
                if (currentToken.replace(
                        "${NetworkConstants.BEARER} ",
                        ""
                    ) == session.token.value
                ) {
                    val body = RefreshTokenRequestJsonAdapter(moshi)
                        .toJson(RefreshTokenRequest(session.refreshToken.value))

                    val request = Request.Builder()
                        .url("${session.baseUrl}/${TaigaApi.REFRESH_ENDPOINT}")
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .build()

                    val refreshResponse = RefreshTokenResponseJsonAdapter(moshi)
                        .fromJson(
                            okHttpClient.newCall(request).execute().body?.string().orEmpty()
                        )
                        ?: throw IllegalStateException("Cannot parse RefreshResponse")

                    session.changeAuthCredentials(
                        refreshResponse.authToken,
                        refreshResponse.refresh
                    )
                }
            }

            return response.request.newBuilder()
                .header(
                    NetworkConstants.AUTHORIZATION,
                    "${NetworkConstants.BEARER} ${session.token.value}"
                )
                .build()
        } catch (e: Exception) {
            Timber.w(e)
            session.changeAuthCredentials("", "")
            return null
        }
        return null
    }
}
