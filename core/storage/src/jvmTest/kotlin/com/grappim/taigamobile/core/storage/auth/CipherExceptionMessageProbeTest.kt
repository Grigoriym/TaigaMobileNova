package com.grappim.taigamobile.core.storage.auth

import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

// Probes AndroidKeystoreTokenCipher's ERROR-logged exception types (AndroidKeyStore itself isn't available on the JVM).
class CipherExceptionMessageProbeTest {

    private val marker = "s3nsitive-plaintext-token-marker"

    @Test
    fun `GCM tag mismatch exception message does not contain the plaintext`() {
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val iv = ByteArray(12)
        val encryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ciphertext = encryptCipher.doFinal(marker.encodeToByteArray())
        ciphertext[0] = (ciphertext[0].toInt() xor 0xFF).toByte()

        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        decryptCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val exception = assertFailsWith<GeneralSecurityException> { decryptCipher.doFinal(ciphertext) }

        assertFalse(exception.message.orEmpty().contains(marker))
    }

    @Test
    fun `Base64 decode failure exception message does not contain the raw input`() {
        val invalidBase64 = "$marker!!!not-valid-base64###"

        val exception = assertFailsWith<IllegalArgumentException> { Base64.decode(invalidBase64) }

        assertFalse(exception.message.orEmpty().contains(marker))
    }
}
