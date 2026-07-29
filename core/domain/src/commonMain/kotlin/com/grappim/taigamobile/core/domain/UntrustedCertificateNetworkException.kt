package com.grappim.taigamobile.core.domain

class UntrustedCertificateNetworkException(val pendingCertTrust: PendingCertTrust) : PlatformIOException()
