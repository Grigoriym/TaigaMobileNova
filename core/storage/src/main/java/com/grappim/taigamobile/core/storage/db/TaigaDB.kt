package com.grappim.taigamobile.core.storage.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.grappim.taigamobile.core.storage.db.dao.ProjectDao
import com.grappim.taigamobile.core.storage.db.dao.SprintDao
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity
import com.grappim.taigamobile.core.storage.db.entities.SprintEntity
import com.grappim.taigamobile.core.storage.db.entities.WorkItemEntity

@Database(
    entities = [
        ProjectEntity::class,
        SprintEntity::class,
        WorkItemEntity::class
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
@TypeConverters(
    TaigaPermissionConverter::class,
    CacheTypeConverters::class
)
abstract class TaigaDB : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun sprintDao(): SprintDao
    abstract fun workItemDao(): WorkItemDao
}
