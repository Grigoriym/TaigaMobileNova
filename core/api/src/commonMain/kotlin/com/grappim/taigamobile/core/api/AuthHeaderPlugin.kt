package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.util.AttributeKey

class AuthHeaderPlugin(private val authStorage: AuthStorage, private val appInfoProvider: AppInfoProvider) {

    class Config {
        lateinit var authStorage: AuthStorage
        lateinit var appInfoProvider: AppInfoProvider
    }

    companion object Plugin : HttpClientPlugin<Config, AuthHeaderPlugin> {
        override val key: AttributeKey<AuthHeaderPlugin> = AttributeKey("AuthHeaderPlugin")

        override fun prepare(block: Config.() -> Unit): AuthHeaderPlugin {
            val config = Config().apply(block)
            return AuthHeaderPlugin(config.authStorage, config.appInfoProvider)
        }

        override fun install(plugin: AuthHeaderPlugin, scope: HttpClient) {
            scope.plugin(HttpSend).intercept { request ->
                request.headers[ApiConstants.USER_AGENT] =
                    "TaigaMobileNova/${plugin.appInfoProvider.getVersionName()}"

                val token = plugin.authStorage.getToken()
                if (token.isNotEmpty()) {
                    request.headers[ApiConstants.AUTHORIZATION] =
                        ApiConstants.generateBearerToken(token)
                }

                execute(request)
            }
        }
    }
}
