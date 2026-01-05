package com.grappim.taigamobile.core.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity

@Database(
    entities = [
        ProjectEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    TaigaPermissionConverter::class
)
abstract class TaigaDB : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}
