package com.grappim.taigamobile.core.storage.cleaner

import com.grappim.taigamobile.core.storage.AuthStorage
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import javax.inject.Inject

class DataCleanerImpl @Inject constructor(
    private val authStorage: AuthStorage,
    private val taigaSessionStorage: TaigaSessionStorage
) : DataCleaner {

    override suspend fun cleanOnGoingBackAfterLogin() {
        authStorage.clear()
        taigaSessionStorage.clearData()
    }
}
