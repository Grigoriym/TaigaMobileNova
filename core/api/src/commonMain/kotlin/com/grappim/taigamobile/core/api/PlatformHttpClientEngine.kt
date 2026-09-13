package com.grappim.taigamobile.core.api

import com.grappim.kit.storage.cert.TrustedCertStorage
import io.ktor.client.engine.HttpClientEngine

expect fun createPlatformHttpClientEngine(trustedCertStorage: TrustedCertStorage): HttpClientEngine
