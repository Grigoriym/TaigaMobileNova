package com.grappim.taigamobile.core.api

import java.math.BigInteger
import java.security.Principal
import java.security.PublicKey
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

/**
 * Minimal [X509Certificate] double for tests. Only [getEncoded] (used for fingerprinting) and
 * [checkValidity] (driven by [notBefore]/[notAfter]) are meaningful — everything else this class
 * doesn't otherwise need is stubbed since [X509Certificate] has no lighter-weight fake in the JDK.
 */
@Suppress("OVERRIDE_DEPRECATION")
class FakeX509Certificate(
    private val encodedBytes: ByteArray,
    private val notBefore: Date = Date(0),
    private val notAfter: Date = Date(Long.MAX_VALUE / 2),
    private val commonName: String = "taiga.example.com",
    private val subjectAlternativeNames: List<List<Any>>? = null
) : X509Certificate() {

    override fun checkValidity() = checkValidity(Date())

    override fun checkValidity(date: Date) {
        if (date.before(notBefore)) throw CertificateNotYetValidException()
        if (date.after(notAfter)) throw CertificateExpiredException()
    }

    override fun getVersion(): Int = 3
    override fun getSerialNumber(): BigInteger = BigInteger.ONE
    override fun getIssuerDN(): Principal = Principal { "CN=Fake Issuer" }
    override fun getSubjectDN(): Principal = Principal { "CN=Fake Subject" }

    // The JDK's default getSubjectX500Principal()/getIssuerX500Principal() re-parse the real
    // certificate bytes from getEncoded() (via sun.security.x509.X509CertImpl), which fails for
    // this fake's non-DER encodedBytes — override directly instead of relying on that reparsing.
    override fun getIssuerX500Principal(): X500Principal = X500Principal("CN=Fake Issuer")
    override fun getSubjectX500Principal(): X500Principal = X500Principal("CN=$commonName")
    override fun getSubjectAlternativeNames(): Collection<List<Any>>? = subjectAlternativeNames
    override fun getNotBefore(): Date = notBefore
    override fun getNotAfter(): Date = notAfter
    override fun getTBSCertificate(): ByteArray = encodedBytes
    override fun getSignature(): ByteArray = ByteArray(0)
    override fun getSigAlgName(): String = "SHA256withRSA"
    override fun getSigAlgOID(): String = "1.2.840.113549.1.1.11"
    override fun getSigAlgParams(): ByteArray? = null
    override fun getIssuerUniqueID(): BooleanArray? = null
    override fun getSubjectUniqueID(): BooleanArray? = null
    override fun getKeyUsage(): BooleanArray? = null
    override fun getBasicConstraints(): Int = -1

    override fun hasUnsupportedCriticalExtension(): Boolean = false
    override fun getCriticalExtensionOIDs(): Set<String> = emptySet()
    override fun getNonCriticalExtensionOIDs(): Set<String> = emptySet()
    override fun getExtensionValue(oid: String?): ByteArray? = null

    override fun getEncoded(): ByteArray = encodedBytes
    override fun verify(key: PublicKey?) = Unit
    override fun verify(key: PublicKey?, sigProvider: String?) = Unit
    override fun toString(): String = "FakeX509Certificate"
    override fun getPublicKey(): PublicKey? = null
}
