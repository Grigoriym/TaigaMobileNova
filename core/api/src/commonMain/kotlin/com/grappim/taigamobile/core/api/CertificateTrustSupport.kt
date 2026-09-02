package com.grappim.taigamobile.core.api

// False on iOS: the Darwin engine has no TrustedCertStorage-backed trust manager (docs/revisit.md #33).
expect val supportsCertificateTrustManagement: Boolean
