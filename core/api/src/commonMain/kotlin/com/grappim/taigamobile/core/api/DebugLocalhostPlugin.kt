package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.appinfoapi.DebugLocalHostProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.util.AttributeKey

class DebugLocalhostPlugin(private val debugLocalHostProvider: DebugLocalHostProvider) {

    class Config {
        lateinit var debugLocalHostProvider: DebugLocalHostProvider
    }

    companion object Plugin : HttpClientPlugin<Config, DebugLocalhostPlugin> {
        override val key: AttributeKey<DebugLocalhostPlugin> = AttributeKey("DebugLocalhostPlugin")

        override fun prepare(block: Config.() -> Unit): DebugLocalhostPlugin {
            val config = Config().apply(block)
            return DebugLocalhostPlugin(config.debugLocalHostProvider)
        }

        override fun install(plugin: DebugLocalhostPlugin, scope: HttpClient) {
            scope.plugin(HttpSend).intercept { request ->
                if (request.url.host == "localhost") {
                    val debugHost = plugin.debugLocalHostProvider.getDebugLocalHost()
                    if (debugHost.isNotEmpty()) {
                        val parsed = Url(debugHost)
                        request.url {
                            protocol = URLProtocol.createOrDefault(parsed.protocol.name)
                            host = parsed.host
                            port = parsed.port
                        }
                    }
                }
                execute(request)
            }
        }
    }
}
