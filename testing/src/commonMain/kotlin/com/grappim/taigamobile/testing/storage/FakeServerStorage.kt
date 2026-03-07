package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.server.ServerStorage

class FakeServerStorage(
    override var server: String = "https://taiga.example.com"
) : ServerStorage {
    override fun defineServer(value: String) {
        server = value
    }
}
