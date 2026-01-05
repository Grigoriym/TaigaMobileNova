package com.grappim.taigamobile.core.storage.server

import android.content.Context
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.utils.string
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ServerStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appInfoProvider: AppInfoProvider
) : ServerStorage {

    companion object {
        private const val SERVER_STORAGE_NAME = "taiga_server_storage_name"

        private const val SERVER_KEY = "server_key"
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SERVER_STORAGE_NAME, Context.MODE_PRIVATE)
    }

    override var server: String by sharedPreferences.string(
        key = SERVER_KEY,
        defaultValue = getServerDefaultValue()
    )

    private fun getServerDefaultValue(): String =
        if (appInfoProvider.isDebug() && appInfoProvider.getDebugLocalHost().isNotEmpty()) {
            appInfoProvider.getDebugLocalHost()
        } else {
            "https://api.taiga.io"
        }

    override fun defineServer(value: String) {
        server = value
    }
}
