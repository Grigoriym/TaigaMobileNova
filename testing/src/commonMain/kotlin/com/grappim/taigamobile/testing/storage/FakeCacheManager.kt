package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.cache.CacheManager

class FakeCacheManager : CacheManager {

    var clearAllCacheCalled = false

    override suspend fun cleanExpiredCache(): Unit = error("not used in this test")
    override suspend fun clearProjectCache(projectId: Long): Unit = error("not used in this test")
    override suspend fun clearAllCache() {
        clearAllCacheCalled = true
    }
}
