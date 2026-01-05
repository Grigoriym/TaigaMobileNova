package com.grappim.taigamobile.core.storage.db.wrapper

interface DatabaseWrapper {
    suspend fun clearAllTables()

    fun close()
}
