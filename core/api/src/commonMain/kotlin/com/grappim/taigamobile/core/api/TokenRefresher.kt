package com.grappim.taigamobile.core.api

/**
 * Abstraction for token refresh and logout, so core:api doesn't depend on feature modules.
 * Implemented by the app-level DI.
 */
interface TokenRefresher {
    suspend fun refresh(refreshToken: String): RefreshedTokens
    fun logout()
}
