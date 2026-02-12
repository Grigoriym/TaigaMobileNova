package com.grappim.taigamobile.core.storage.cleaner

import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.core.storage.cache.CacheManager
import javax.inject.Inject

class DataCleanerImpl @Inject constructor(
    private val authStorage: AuthStorage,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val cacheManager: CacheManager
) : DataCleaner {

    override suspend fun cleanOnGoingBackAfterLogin() {
        authStorage.clear()
        taigaSessionStorage.clearData()
        cacheManager.clearAllCache()
    }
}
