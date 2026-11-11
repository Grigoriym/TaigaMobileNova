package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.storage.customheaders.CustomHeadersStorage
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.logging.LoggingConfig
import io.ktor.http.Url
import io.ktor.http.parseUrl

/**
 * Adds the user's reverse-proxy headers to every request bound for the server's own origin, and to
 * nothing else.
 *
 * Hooked on `SendingRequest`, which runs once per *hop*: `HttpRedirect` copies the original
 * request's headers onto each redirected hop, so every hop drops the configured names first and
 * gets them back only while it is still on the server. It also runs after the `HttpSend`
 * interceptors have rewritten the URL, so the URL compared here is the one actually sent.
 */
val CustomHeaders = createClientPlugin("CustomHeaders", ::CustomHeadersConfig) {
    val storage = pluginConfig.storage
    val serverUrl = pluginConfig.serverUrl

    on(SendingRequest) { request, _ ->
        val headers = storage.currentHeaders
        if (headers.isNotEmpty()) {
            headers.forEach { request.headers.remove(it.name) }
            if (isServerOrigin(request.url.build(), serverUrl())) {
                headers.forEach { request.headers.append(it.name, it.value) }
            }
        }
    }
}

/** Scheme, host and port, so an `https` server's headers never go out over plain `http` either. */
internal fun isServerOrigin(requestUrl: Url, serverUrl: String): Boolean {
    val server = serverUrl.takeIf { it.isNotBlank() }?.let(::parseUrl) ?: return false
    return requestUrl.protocol == server.protocol &&
        requestUrl.host.equals(server.host, ignoreCase = true) &&
        requestUrl.port == server.port
}

/**
 * The values are credentials, and `LogLevel.ALL` prints request headers. The predicate runs per
 * header at log time, so it sees whatever names are configured *now*.
 */
internal fun LoggingConfig.sanitizeCustomHeaders(storage: CustomHeadersStorage) {
    sanitizeHeader { name -> storage.currentHeaders.any { it.name.equals(name, ignoreCase = true) } }
}
