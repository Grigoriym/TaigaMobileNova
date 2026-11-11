package com.grappim.taigamobile.core.storage.customheaders

/** Why [CustomHeaderValidator] won't send a row. */
enum class CustomHeaderError {
    MissingName,
    InvalidName,

    /** A header the HTTP stack or the app sets itself — sending a second one would break it. */
    ReservedName,
    DuplicateName,
    InvalidValue
}
