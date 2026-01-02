package com.grappim.taigamobile.core.storage.db.wrapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.storage.db.TaigaDB
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseWrapperImpl @Inject constructor(
    private val db: TaigaDB,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DatabaseWrapper {

    override suspend fun clearAllTables() = withContext(ioDispatcher) {
        db.clearAllTables()
    }

    override fun close() {
        db.close()
    }
}
