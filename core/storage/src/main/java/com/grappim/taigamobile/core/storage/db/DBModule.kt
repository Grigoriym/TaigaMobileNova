package com.grappim.taigamobile.core.storage.db

import android.content.Context
import androidx.room.Room
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.core.storage.db.dao.SprintDao
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
class DBModule {

    @[Provides Singleton]
    fun provideDb(
        @ApplicationContext context: Context,
        infoProvider: AppInfoProvider,
        taigaPermissionConverter: TaigaPermissionConverter,
        cacheTypeConverters: CacheTypeConverters
    ): TaigaDB = Room.databaseBuilder(
        context,
        TaigaDB::class.java,
        "taigamobilenova_${infoProvider.getBuildType()}.db"
    ).addTypeConverter(taigaPermissionConverter)
        .addTypeConverter(cacheTypeConverters)
        .build()

    @[Provides Singleton]
    fun provideProjectDao(db: TaigaDB): ProjectDao = db.projectDao()

    @[Provides Singleton]
    fun provideSprintDao(db: TaigaDB): SprintDao = db.sprintDao()

    @[Provides Singleton]
    fun provideWorkItemDao(db: TaigaDB): WorkItemDao = db.workItemDao()
}
