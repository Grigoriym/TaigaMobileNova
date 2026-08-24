package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider

/**
 * Fakes for the collaborators of this module's Ktor plugins.
 *
 * They live here rather than in `:testing` on purpose: `AppInfoProvider`, [BaseUrlProvider] and
 * [TokenRefresher] are only faked by this module's own tests, and `:testing` would have to gain a
 * dependency on `:core:api` — which every other module already depends on — to host them. The
 * `FakeX509TrustManager` in `jvmTest` sets the same precedent.
 */
class FakeAppInfoProvider(
    var versionNameToReturn: String = "1.0.0",
    var debugLocalHostToReturn: String = "",
    var isDebugToReturn: Boolean = true
) : AppInfoProvider {
    override fun getAppInfo(): String = "fake"
    override fun isDebug(): Boolean = isDebugToReturn
    override fun isFdroidBuild(): Boolean = false
    override fun getVersionName(): String = versionNameToReturn
    override fun getDebugLocalHost(): String = debugLocalHostToReturn
    override fun getBuildType(): String = "debug"
}

class FakeBaseUrlProvider(var baseUrlToReturn: String = "") : BaseUrlProvider {
    override fun getBaseUrl(): String = baseUrlToReturn
}

class FakeTokenRefresher(
    var refreshedTokens: RefreshedTokens = RefreshedTokens("newToken", "newRefresh"),
    var refreshThrows: Throwable? = null
) : TokenRefresher {

    var refreshCallCount = 0
    var refreshArgument: String? = null
    var logoutCallCount = 0

    override suspend fun refresh(refreshToken: String): RefreshedTokens {
        refreshCallCount++
        refreshArgument = refreshToken
        refreshThrows?.let { throw it }
        return refreshedTokens
    }

    override fun logout() {
        logoutCallCount++
    }
}
