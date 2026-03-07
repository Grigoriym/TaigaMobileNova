package com.grappim.taigamobile.core.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.util.AttributeKey

class HostSelectionPlugin(private val baseUrlProvider: BaseUrlProvider) {

    class Config {
        lateinit var baseUrlProvider: BaseUrlProvider
    }

    companion object Plugin : HttpClientPlugin<Config, HostSelectionPlugin> {
        override val key: AttributeKey<HostSelectionPlugin> = AttributeKey("HostSelectionPlugin")

        override fun prepare(block: Config.() -> Unit): HostSelectionPlugin {
            val config = Config().apply(block)
            return HostSelectionPlugin(config.baseUrlProvider)
        }

        override fun install(plugin: HostSelectionPlugin, scope: HttpClient) {
            scope.plugin(HttpSend).intercept { request ->
                val baseUrl = plugin.baseUrlProvider.getBaseUrl()
                if (baseUrl.isNotEmpty()) {
                    val parsed = Url(baseUrl)
                    request.url {
                        protocol = URLProtocol.createOrDefault(parsed.protocol.name)
                        host = parsed.host
                        port = parsed.port
                    }
                }
                execute(request)
            }
        }
    }
}
