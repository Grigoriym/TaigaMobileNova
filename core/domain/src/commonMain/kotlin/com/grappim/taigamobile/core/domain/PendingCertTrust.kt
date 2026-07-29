package com.grappim.taigamobile.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class PendingCertTrust(
    val host: String,
    val subject: String,
    val issuer: String,
    val notBefore: String,
    val notAfter: String,
    val sha256Fingerprint: String
)
