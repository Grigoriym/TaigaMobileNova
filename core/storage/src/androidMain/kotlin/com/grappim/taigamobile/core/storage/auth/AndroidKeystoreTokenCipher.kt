package com.grappim.taigamobile.core.storage.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "taiga_auth_token_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_IV_LENGTH_BYTES = 12
private const val GCM_TAG_LENGTH_BITS = 128
private const val CIPHERTEXT_PREFIX = "v1:"

/**
 * A value with no [CIPHERTEXT_PREFIX] is a plaintext value written before this cipher existed —
 * [decrypt] passes it through unchanged rather than failing, and it is migrated to ciphertext the
 * next time it's written (e.g. the next token refresh).
 */
class AndroidKeystoreTokenCipher : TokenCipher {

    private val secretKey: SecretKey by lazy { getOrCreateKey() }

    override fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val ciphertext = cipher.doFinal(plaintext.encodeToByteArray())
        return CIPHERTEXT_PREFIX + Base64.encode(cipher.iv + ciphertext)
    }

    override fun decrypt(value: String): String {
        if (!value.startsWith(CIPHERTEXT_PREFIX)) return value
        return try {
            val payload = Base64.decode(value.removePrefix(CIPHERTEXT_PREFIX))
            val iv = payload.copyOfRange(0, GCM_IV_LENGTH_BYTES)
            val ciphertext = payload.copyOfRange(GCM_IV_LENGTH_BYTES, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            cipher.doFinal(ciphertext).decodeToString()
        } catch (e: GeneralSecurityException) {
            logcat(LogPriority.ERROR, throwable = e) { "failed to decrypt stored token" }
            ""
        } catch (e: IllegalArgumentException) {
            logcat(LogPriority.ERROR, throwable = e) { "stored token is not valid ciphertext" }
            ""
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
