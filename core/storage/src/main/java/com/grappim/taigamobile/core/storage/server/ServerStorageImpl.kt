package com.grappim.taigamobile.core.storage.server

import android.content.Context
import com.grappim.taigamobile.core.storage.utils.string
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ServerStorageImpl @Inject constructor(@ApplicationContext private val context: Context) : ServerStorage {

    companion object {
        private const val SERVER_STORAGE_NAME = "taiga server storage name"

        private const val SERVER_KEY = "server key"
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SERVER_STORAGE_NAME, Context.MODE_PRIVATE)
    }

    override var server: String by sharedPreferences.string(
        key = SERVER_KEY,
        defaultValue = "https://api.taiga.io"
    )

    override fun defineServer(value: String) {
        server = value
    }
}
