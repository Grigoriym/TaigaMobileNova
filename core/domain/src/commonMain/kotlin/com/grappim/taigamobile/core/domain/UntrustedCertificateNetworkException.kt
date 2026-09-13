package com.grappim.taigamobile.core.domain

import com.grappim.kit.domain.PendingCertTrust

class UntrustedCertificateNetworkException(val pendingCertTrust: PendingCertTrust) : PlatformIOException()
