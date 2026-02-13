package com.grappim.taigamobile.core.storage.cleaner

import com.grappim.taigamobile.core.storage.KmpTaigaSessionStorage
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.core.storage.cache.CacheManager
import org.koin.core.annotation.Single

@Single(binds = [DataCleaner::class])
class DataCleanerImpl(
    private val authStorage: AuthStorage,
    private val taigaSessionStorage: KmpTaigaSessionStorage,
    private val cacheManager: CacheManager
) : DataCleaner {

    override suspend fun cleanOnGoingBackAfterLogin() {
        authStorage.clear()
        taigaSessionStorage.clearData()
        cacheManager.clearAllCache()
    }
}
