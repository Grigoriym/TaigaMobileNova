package com.grappim.taigamobile.core.storage.db

import android.content.Context
import androidx.room.Room
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
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
        taigaPermissionConverter: TaigaPermissionConverter
    ): TaigaDB = Room.databaseBuilder(
        context,
        TaigaDB::class.java,
        "taigamobilenova_${infoProvider.getBuildType()}.db"
    ).addTypeConverter(taigaPermissionConverter)
        .build()

    @[Provides Singleton]
    fun provideProjectDao(db: TaigaDB): ProjectDao = db.projectDao()
}
