package com.grappim.taigamobile.core.storage.cache

import com.grappim.taigamobile.core.storage.db.dao.SprintDao
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import org.koin.core.annotation.Factory

/**
 * Default cache TTL: 24 hours in milliseconds.
 */
private const val DEFAULT_CACHE_TTL_MS = 24 * 60 * 60 * 1000L

@Factory(binds = [CacheManager::class])
class CacheManagerImpl(private val sprintDao: SprintDao, private val workItemDao: WorkItemDao) :
    CacheManager {

    override suspend fun cleanExpiredCache() {
        val expirationThreshold = System.currentTimeMillis() - DEFAULT_CACHE_TTL_MS
        sprintDao.deleteOlderThan(expirationThreshold)
        workItemDao.deleteOlderThan(expirationThreshold)
    }

    override suspend fun clearProjectCache(projectId: Long) {
        sprintDao.deleteByProjectId(projectId)
        workItemDao.deleteByProjectId(projectId)
    }

    override suspend fun clearAllCache() {
        // Delete everything by using a future timestamp
        val futureTimestamp = System.currentTimeMillis() + 1000
        sprintDao.deleteOlderThan(futureTimestamp)
        workItemDao.deleteOlderThan(futureTimestamp)
    }
}
