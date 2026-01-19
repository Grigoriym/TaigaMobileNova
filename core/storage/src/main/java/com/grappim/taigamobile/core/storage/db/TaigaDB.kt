package com.grappim.taigamobile.core.storage.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity

@Database(
    entities = [
        ProjectEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(
    TaigaPermissionConverter::class
)
abstract class TaigaDB : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}
