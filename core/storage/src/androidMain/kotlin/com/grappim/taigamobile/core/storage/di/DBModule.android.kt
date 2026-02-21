package com.grappim.taigamobile.core.storage.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.db.TaigaDB
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [DBModule::class])
@Configuration
actual class PlatformDBModule {

    @Single
    fun provideDatabaseBuilder(context: Context, infoProvider: AppInfoProvider): RoomDatabase.Builder<TaigaDB> =
        Room.databaseBuilder<TaigaDB>(
            context = context.applicationContext,
            name = context.applicationContext
                .getDatabasePath("taigamobilenova_${infoProvider.getBuildType()}.db")
                .absolutePath
        )
}
