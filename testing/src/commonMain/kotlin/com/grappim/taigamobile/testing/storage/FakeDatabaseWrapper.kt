package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.db.wrapper.DatabaseWrapper

class FakeDatabaseWrapper : DatabaseWrapper {
    var clearAllTablesCalled = false

    override suspend fun clearAllTables() {
        clearAllTablesCalled = true
    }

    override fun close() {}
}
