package com.grappim.taigamobile.core.storage.server

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.di.createDataStore
import com.grappim.taigamobile.core.storage.platform.appDataDir
import org.koin.core.annotation.Single
import java.io.File

@Single(binds = [ServerStorage::class])
class ServerStorageImpl(appInfoProvider: AppInfoProvider) :
    ServerStorage by DataStoreServerStorage(
        dataStore = createDataStore {
            File(appDataDir(), SERVER_STORAGE_FILE_NAME).absolutePath
        },
        appInfoProvider = appInfoProvider
    )
