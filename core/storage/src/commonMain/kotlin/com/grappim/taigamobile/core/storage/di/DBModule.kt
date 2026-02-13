package com.grappim.taigamobile.core.storage.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.grappim.taigamobile.core.storage.db.CacheTypeConverters
import com.grappim.taigamobile.core.storage.db.TaigaDB
import com.grappim.taigamobile.core.storage.db.TaigaPermissionConverter
import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.core.storage.db.dao.SprintDao
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DbJsonQualifier

@Module
@Configuration
class DBModule {

    @Single
    fun provideDb(
        builder: RoomDatabase.Builder<TaigaDB>,
        taigaPermissionConverter: TaigaPermissionConverter,
        cacheTypeConverters: CacheTypeConverters,
    ): TaigaDB = builder
        .addTypeConverter(taigaPermissionConverter)
        .addTypeConverter(cacheTypeConverters)
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .build()

    @Single
    fun provideProjectDao(db: TaigaDB): ProjectDao = db.projectDao()

    @Single
    fun provideSprintDao(db: TaigaDB): SprintDao = db.sprintDao()

    @Single
    fun provideWorkItemDao(db: TaigaDB): WorkItemDao = db.workItemDao()

    @[Single DbJsonQualifier]
    fun provideDbJson(): Json = Json {
        isLenient = true
        prettyPrint = false
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}

@Module
expect class PlatformDBModule