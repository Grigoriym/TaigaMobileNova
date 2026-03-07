@file:OptIn(ExperimentalForeignApi::class)

package com.grappim.taigamobile.core.storage.server

import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.di.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Single(binds = [ServerStorage::class])
class ServerStorageImpl(appInfoProvider: AppInfoProvider) :
    ServerStorage by DataStoreServerStorage(
        dataStore = createDataStore {
            val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )
            requireNotNull(documentDirectory).path + "/$SERVER_STORAGE_FILE_NAME"
        },
        appInfoProvider = appInfoProvider
    )
