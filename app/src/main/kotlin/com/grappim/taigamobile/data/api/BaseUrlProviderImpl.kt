package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.api.ApiConstants
import com.grappim.taigamobile.core.api.BaseUrlProvider
import com.grappim.taigamobile.core.storage.server.ServerStorage
import javax.inject.Inject

class BaseUrlProviderImpl @Inject constructor(private val serverStorage: ServerStorage) :
    BaseUrlProvider {
    override fun getBaseUrl() = "${serverStorage.server}/${ApiConstants.API_PREFIX}/"
}
