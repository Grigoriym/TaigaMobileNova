package com.grappim.taigamobile.core.storage.db

actual suspend fun TaigaDB.clearAllTablesKmp() {
    projectDao().deleteAll()
    sprintDao().deleteAll()
    workItemDao().deleteAll()
}
