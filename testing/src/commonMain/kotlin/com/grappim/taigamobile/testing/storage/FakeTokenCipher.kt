package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.auth.TokenCipher

class FakeTokenCipher : TokenCipher {
    var decryptResult: ((String) -> String)? = null

    override fun encrypt(plaintext: String): String = "ENC:$plaintext"

    override fun decrypt(value: String): String {
        decryptResult?.let { return it(value) }
        return value.removePrefix("ENC:")
    }
}
