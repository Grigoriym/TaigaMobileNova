package com.grappim.taigamobile.core.api

import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.parseUrl

/**
 * A response that came from whatever guards the server, not from Taiga.
 *
 * Taiga answers its own `401`s with a `WWW-Authenticate: Bearer` challenge, so a `401` without one
 * — or any `407` — is a proxy asking for credentials. A redirect to an absolute URL on another host
 * is a proxy sending the app to its own login page.
 *
 * @param requestHost the host the request was actually sent to — a redirect that stays on it
 * (an `http` → `https` upgrade, say) is not a login page.
 */
fun isProxyRejection(statusCode: Int, wwwAuthenticate: String?, location: String?, requestHost: String): Boolean =
    statusCode == HttpStatusCode.ProxyAuthenticationRequired.value ||
        (statusCode == HttpStatusCode.Unauthorized.value && !isTaigaChallenge(wwwAuthenticate)) ||
        isRedirectOffHost(statusCode, location, requestHost)

internal fun HttpResponse.isProxyRejection(): Boolean = isProxyRejection(
    statusCode = status.value,
    wwwAuthenticate = headers[HttpHeaders.WWWAuthenticate],
    location = headers[HttpHeaders.Location],
    requestHost = call.request.url.host
)

private fun isTaigaChallenge(wwwAuthenticate: String?): Boolean =
    wwwAuthenticate?.trimStart()?.startsWith(ApiConstants.BEARER, ignoreCase = true) == true

private fun isRedirectOffHost(statusCode: Int, location: String?, requestHost: String): Boolean {
    if (statusCode !in HTTP_REDIRECT_RANGE || location == null) return false
    val isAbsolute = location.contains(SCHEME_SEPARATOR) || location.startsWith(PROTOCOL_RELATIVE)
    if (!isAbsolute) return false
    val target = parseUrl(if (location.startsWith(PROTOCOL_RELATIVE)) "http:$location" else location)
    return target != null && !target.host.equals(requestHost, ignoreCase = true)
}

private val HTTP_REDIRECT_RANGE = 300..399
private const val SCHEME_SEPARATOR = "://"
private const val PROTOCOL_RELATIVE = "//"
