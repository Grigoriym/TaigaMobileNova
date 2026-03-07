package com.grappim.taigamobile.core.storage.server

interface ServerStorage {
    val server: String
    fun defineServer(value: String)
}
