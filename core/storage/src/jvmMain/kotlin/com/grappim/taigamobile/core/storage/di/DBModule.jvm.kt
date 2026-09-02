package com.grappim.taigamobile.core.storage.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.grappim.taigamobile.core.storage.db.TaigaDB
import com.grappim.taigamobile.core.storage.platform.appDataDir
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.io.File

@Module(includes = [DBModule::class])
@Configuration
actual class PlatformDBModule {
    @Single
    fun getDatabaseBuilder(): RoomDatabase.Builder<TaigaDB> {
        val dbFile = File(appDataDir(), "taigamobilenova.db")
        return Room.databaseBuilder<TaigaDB>(
            name = dbFile.absolutePath
        )
    }
}
