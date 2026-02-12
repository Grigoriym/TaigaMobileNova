package com.grappim.taigamobile.core.storage.cache

/**
 * Manages cached data lifecycle: TTL expiration, cleanup, and invalidation.
 */
interface CacheManager {
    /**
     * Delete all cached data older than the TTL threshold.
     * Should be called periodically (e.g., on app start).
     */
    suspend fun cleanExpiredCache()

    /**
     * Delete all cached data for a specific project.
     * Useful when switching projects or forcing refresh.
     */
    suspend fun clearProjectCache(projectId: Long)

    /**
     * Delete all cached data.
     * Called on logout.
     */
    suspend fun clearAllCache()
}
