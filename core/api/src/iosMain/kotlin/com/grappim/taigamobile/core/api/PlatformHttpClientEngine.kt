package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.storage.cert.TrustedCertStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformHttpClientEngine(trustedCertStorage: TrustedCertStorage): HttpClientEngine = Darwin.create()
