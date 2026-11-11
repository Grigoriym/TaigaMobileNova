package com.grappim.taigamobile.core.storage.customheaders

/**
 * Enforces OkHttp's rule, the stricter of the engines: it throws at request time, after the user
 * has left the screen that could have said why.
 *
 * A row with both fields blank is not an error: it is an unfilled row, and it is never sent.
 */
object CustomHeaderValidator {

    /** One entry per row of [headers], in order — `null` for a row that can be sent. */
    fun validate(headers: List<CustomHeader>): List<CustomHeaderError?> {
        val seen = mutableSetOf<String>()
        return headers.map { header ->
            if (header.isBlank()) return@map null
            validateName(header.name) ?: validateValue(header.value)
                ?: CustomHeaderError.DuplicateName.takeUnless { seen.add(header.name.lowercase()) }
        }
    }

    private fun validateName(name: String): CustomHeaderError? = when {
        name.isEmpty() -> CustomHeaderError.MissingName
        name.any { it.code !in NAME_CHARS || it in DELIMITERS } -> CustomHeaderError.InvalidName
        RESERVED_NAMES.any { it.equals(name, ignoreCase = true) } -> CustomHeaderError.ReservedName
        else -> null
    }

    private fun validateValue(value: String): CustomHeaderError? =
        CustomHeaderError.InvalidValue.takeIf { value.any { it != '\t' && it.code !in VALUE_CHARS } }

    private val NAME_CHARS = 0x21..0x7E
    private val VALUE_CHARS = 0x20..0x7E
    private const val DELIMITERS = "\"(),/:;<=>?@[\\]{}"

    /**
     * `Transfer-Encoding`/`Upgrade` are Ktor's `UnsafeHeadersList` (the engine throws on them).
     * `Authorization` carries Taiga's own bearer token and `User-Agent` is set per request, so a
     * proxy that authenticates through either can't be satisfied by an extra header.
     */
    private val RESERVED_NAMES = listOf(
        "Host",
        "Content-Length",
        "Content-Type",
        "Transfer-Encoding",
        "Upgrade",
        "Connection",
        "Cookie",
        "Authorization",
        "User-Agent"
    )
}
