package com.grappim.taigamobile.core.storage.db.wrapper

import com.grappim.taigamobile.core.asynckmp.IoDispatcher
import com.grappim.taigamobile.core.storage.db.TaigaDB
import com.grappim.taigamobile.core.storage.db.clearAllTablesKmp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single(binds = [DatabaseWrapper::class])
class DatabaseWrapperImpl(private val db: TaigaDB, @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher) :
    DatabaseWrapper {

    override suspend fun clearAllTables() = withContext(ioDispatcher) {
        db.clearAllTablesKmp()
    }

    override fun close() {
        db.close()
    }
}
