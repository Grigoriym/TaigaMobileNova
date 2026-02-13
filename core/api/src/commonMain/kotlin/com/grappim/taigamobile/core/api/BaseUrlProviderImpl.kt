package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.storage.server.ServerStorage
import org.koin.core.annotation.Factory

@Factory(binds = [BaseUrlProvider::class])
class BaseUrlProviderImpl(private val serverStorage: ServerStorage) : BaseUrlProvider {
    override fun getBaseUrl() = "${serverStorage.server}/${ApiConstants.API_PREFIX}/"
}