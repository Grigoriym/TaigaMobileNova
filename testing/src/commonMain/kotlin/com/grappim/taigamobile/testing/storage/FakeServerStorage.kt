package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.server.ServerStorage

class FakeServerStorage : ServerStorage {
    override val server: String = "https://taiga.example.com"
    override fun defineServer(value: String) {}
}
