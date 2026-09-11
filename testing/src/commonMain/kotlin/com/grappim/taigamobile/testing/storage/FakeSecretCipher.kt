package com.grappim.taigamobile.testing.storage

import com.grappim.kit.storage.SecretCipher

class FakeSecretCipher : SecretCipher {
    var decryptResult: ((String) -> String?)? = null

    override fun encrypt(value: String): String = "ENC:$value"

    override fun decrypt(value: String): String? {
        decryptResult?.let { return it(value) }
        return value.removePrefix("ENC:")
    }
}
