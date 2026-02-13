package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class AuthHttpClient

@Module
@Configuration
@ComponentScan
class KmpNetworkModule {
    @Single
    fun provideJson(): Json = Json {
        isLenient = true
        prettyPrint = false
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @[Single AuthHttpClient]
    fun provideAuthHttpClient(
        httpJson: Json,
        baseUrlProvider: BaseUrlProvider,
        appInfoProvider: AppInfoProvider
    ): HttpClient = HttpClient {
        expectSuccess = false
        defaultRequest {
            contentType(ContentType.Application.Json)
            url(baseUrlProvider.getBaseUrl())
        }
        install(ContentNegotiation) {
            json(httpJson)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    logcat(tag = "KtorAuth") { message }
                }
            }
            level = LogLevel.ALL
        }
        install(ErrorMappingPlugin) {
            json = httpJson
        }
        install(HostSelectionPlugin) {
            this.baseUrlProvider = baseUrlProvider
        }
        install(DebugLocalhostPlugin) {
            this.appInfoProvider = appInfoProvider
        }
    }

    @[Single]
    fun provideCommonHttpClient(
        httpJson: Json,
        baseUrlProvider: BaseUrlProvider,
        authStorage: AuthStorage,
        appInfoProvider: AppInfoProvider,
        tokenRefresher: TokenRefresher
    ): HttpClient = HttpClient {
        expectSuccess = false
        defaultRequest {
            contentType(ContentType.Application.Json)
            url(baseUrlProvider.getBaseUrl())
        }
        install(ContentNegotiation) {
            json(httpJson)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    logcat(tag = "Ktor") { message }
                }
            }
            level = LogLevel.ALL
        }
        install(ErrorMappingPlugin) {
            json = httpJson
        }
        install(HostSelectionPlugin) {
            this.baseUrlProvider = baseUrlProvider
        }
        install(AuthHeaderPlugin) {
            this.authStorage = authStorage
            this.appInfoProvider = appInfoProvider
        }
        install(TokenRefreshPlugin) {
            this.authStorage = authStorage
            this.tokenRefresher = tokenRefresher
        }
        install(DebugLocalhostPlugin) {
            this.appInfoProvider = appInfoProvider
        }
    }
}
