package com.grappim.taigamobile.core.storage.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.grappim.taigamobile.core.storage.db.TaigaDB
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module(includes = [DBModule::class])
@Configuration
actual class PlatformDBModule {
    @Single
    fun getDatabaseBuilder(): RoomDatabase.Builder<TaigaDB> {
        val dbFile = documentDirectory() + "/taigamobilenova.db"
        return Room.databaseBuilder<TaigaDB>(
            name = dbFile
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        return requireNotNull(documentDirectory?.path)
    }
}
