package com.grappim.taigamobile.core.storage.auth

interface TokenCipher {
    fun encrypt(plaintext: String): String
    fun decrypt(value: String): String
}

class NoopTokenCipher : TokenCipher {
    override fun encrypt(plaintext: String): String = plaintext
    override fun decrypt(value: String): String = value
}
